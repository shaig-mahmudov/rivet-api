package com.engine.taskmanagement.task.dependency.service.implementation;

import com.engine.taskmanagement.auth.service.CurrentUserService;
import com.engine.taskmanagement.common.exception.BadRequestException;
import com.engine.taskmanagement.common.exception.DuplicateResourceException;
import com.engine.taskmanagement.common.exception.ForbiddenException;
import com.engine.taskmanagement.common.exception.ResourceNotFoundException;
import com.engine.taskmanagement.project.entity.Project;
import com.engine.taskmanagement.task.activity.service.abstraction.TaskActivityService;
import com.engine.taskmanagement.task.dependency.dto.response.TaskDependencyResponse;
import com.engine.taskmanagement.task.dependency.entity.TaskDependency;
import com.engine.taskmanagement.task.dependency.repository.TaskDependencyRepository;
import com.engine.taskmanagement.task.dependency.service.abstraction.TaskDependencyService;
import com.engine.taskmanagement.task.dto.response.TaskResponse;
import com.engine.taskmanagement.task.entity.Task;
import com.engine.taskmanagement.task.mapper.TaskMapper;
import com.engine.taskmanagement.task.repository.TaskRepository;
import com.engine.taskmanagement.user.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
public class TaskDependencyServiceImpl implements TaskDependencyService {

    private final TaskDependencyRepository taskDependencyRepository;
    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;
    private final CurrentUserService currentUserService;
    private final TaskActivityService taskActivityService;

    public TaskDependencyServiceImpl(
            TaskDependencyRepository taskDependencyRepository,
            TaskRepository taskRepository,
            TaskMapper taskMapper,
            CurrentUserService currentUserService,
            TaskActivityService taskActivityService
    ) {
        this.taskDependencyRepository = taskDependencyRepository;
        this.taskRepository = taskRepository;
        this.taskMapper = taskMapper;
        this.currentUserService = currentUserService;
        this.taskActivityService = taskActivityService;
    }

    @Override
    @Transactional
    public TaskDependencyResponse addDependency(Long taskId, Long dependsOnTaskId) {
        Task task = findTask(taskId);
        Task dependsOnTask = findTask(dependsOnTaskId);
        User currentUser = currentUserService.getCurrentUser();

        requireTaskAccess(task, currentUser);
        requireTaskAccess(dependsOnTask, currentUser);
        validateDependency(task, dependsOnTask);

        TaskDependency dependency = new TaskDependency();
        dependency.setTask(task);
        dependency.setDependsOnTask(dependsOnTask);
        dependency.setCreatedBy(currentUser);

        TaskDependency savedDependency = taskDependencyRepository.save(dependency);
        taskActivityService.recordDependencyAdded(task, currentUser, dependsOnTask);
        return toResponse(savedDependency);
    }

    @Override
    @Transactional
    public void removeDependency(Long taskId, Long dependsOnTaskId) {
        Task task = findTask(taskId);
        Task dependsOnTask = findTask(dependsOnTaskId);
        User currentUser = currentUserService.getCurrentUser();

        requireTaskAccess(task, currentUser);
        requireTaskAccess(dependsOnTask, currentUser);

        TaskDependency dependency = taskDependencyRepository
                .findByTaskIdAndDependsOnTaskId(taskId, dependsOnTaskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task dependency not found"));

        taskDependencyRepository.delete(dependency);
        taskActivityService.recordDependencyRemoved(task, currentUser, dependsOnTask);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskDependencyResponse> listDependencies(Long taskId) {
        Task task = findTask(taskId);
        requireTaskAccess(task, currentUserService.getCurrentUser());

        return taskDependencyRepository.findByTaskIdOrderByCreatedAtAscIdAsc(taskId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponse> listBlockedTasks(Long taskId) {
        Task task = findTask(taskId);
        User currentUser = currentUserService.getCurrentUser();
        requireTaskAccess(task, currentUser);

        return taskDependencyRepository.findByDependsOnTaskIdOrderByCreatedAtAscIdAsc(taskId).stream()
                .map(TaskDependency::getTask)
                .filter(blockedTask -> canAccessTask(blockedTask, currentUser))
                .map(taskMapper::toResponse)
                .toList();
    }

    private void validateDependency(Task task, Task dependsOnTask) {
        if (task.getId().equals(dependsOnTask.getId())) {
            throw new BadRequestException("A task cannot depend on itself");
        }
        if (!Objects.equals(projectId(task), projectId(dependsOnTask))) {
            throw new BadRequestException("Dependent tasks must belong to the same project");
        }
        if (taskDependencyRepository.existsByTaskIdAndDependsOnTaskId(task.getId(), dependsOnTask.getId())) {
            throw new DuplicateResourceException("Task dependency already exists");
        }
        if (hasDependencyPath(dependsOnTask.getId(), task.getId(), new HashSet<>())) {
            throw new BadRequestException("Circular task dependency is not allowed");
        }
    }

    private boolean hasDependencyPath(Long fromTaskId, Long targetTaskId, Set<Long> visitedTaskIds) {
        if (fromTaskId.equals(targetTaskId)) {
            return true;
        }
        if (!visitedTaskIds.add(fromTaskId)) {
            return false;
        }
        return taskDependencyRepository.findByTaskId(fromTaskId).stream()
                .map(dependency -> dependency.getDependsOnTask().getId())
                .anyMatch(nextTaskId -> hasDependencyPath(nextTaskId, targetTaskId, visitedTaskIds));
    }

    private Task findTask(Long taskId) {
        return taskRepository.findByIdAndDeletedAtIsNull(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));
    }

    private TaskDependencyResponse toResponse(TaskDependency dependency) {
        Task dependsOnTask = dependency.getDependsOnTask();
        TaskDependencyResponse response = new TaskDependencyResponse();
        response.setId(dependency.getId());
        response.setTaskId(dependency.getTask().getId());
        response.setDependsOnTaskId(dependsOnTask.getId());
        response.setDependsOnTaskTitle(dependsOnTask.getTitle());
        response.setDependsOnTaskStatus(dependsOnTask.getStatus());
        response.setCreatedById(dependency.getCreatedBy().getId());
        response.setCreatedAt(dependency.getCreatedAt());
        return response;
    }

    private Long projectId(Task task) {
        return task.getProject() == null ? null : task.getProject().getId();
    }

    private void requireTaskAccess(Task task, User currentUser) {
        if (canAccessTask(task, currentUser)) {
            return;
        }
        throw new ForbiddenException("You can only access tasks assigned to you or owned through your projects");
    }

    private boolean canAccessTask(Task task, User currentUser) {
        if (currentUserService.isAdmin(currentUser)) {
            return true;
        }
        return isAssignee(task, currentUser) || isProjectOwner(task.getProject(), currentUser);
    }

    private boolean isAssignee(Task task, User currentUser) {
        return task.getAssignee() != null && task.getAssignee().getId().equals(currentUser.getId());
    }

    private boolean isProjectOwner(Project project, User currentUser) {
        return project != null && project.getOwner() != null && project.getOwner().getId().equals(currentUser.getId());
    }
}
