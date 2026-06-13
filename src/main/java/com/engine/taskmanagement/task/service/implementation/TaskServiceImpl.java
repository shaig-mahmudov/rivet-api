package com.engine.taskmanagement.task.service.implementation;

import com.engine.taskmanagement.auth.service.CurrentUserService;
import com.engine.taskmanagement.common.exception.BadRequestException;
import com.engine.taskmanagement.common.exception.ForbiddenException;
import com.engine.taskmanagement.common.exception.ResourceNotFoundException;
import com.engine.taskmanagement.project.entity.Project;
import com.engine.taskmanagement.project.repository.ProjectRepository;
import com.engine.taskmanagement.task.dto.request.*;
import com.engine.taskmanagement.task.dto.response.TaskResponse;
import com.engine.taskmanagement.task.entity.Task;
import com.engine.taskmanagement.task.mapper.TaskMapper;
import com.engine.taskmanagement.task.repository.TaskRepository;
import com.engine.taskmanagement.task.service.abstraction.TaskService;
import com.engine.taskmanagement.task.specification.TaskSpecification;
import com.engine.taskmanagement.user.entity.User;
import com.engine.taskmanagement.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final TaskMapper taskMapper;
    private final CurrentUserService currentUserService;


    public TaskServiceImpl(
            TaskRepository taskRepository,
            ProjectRepository projectRepository,
            UserRepository userRepository,
            TaskMapper taskMapper,
            CurrentUserService currentUserService
    ) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.taskMapper = taskMapper;
        this.currentUserService = currentUserService;
    }

    @Override
    public TaskResponse createTask(CreateTaskRequest request) {
        User currentUser = currentUserService.getCurrentUser();
        Task task = taskMapper.toEntity(request);

        if (request.getProjectId() != null) {
            Project project = projectRepository.findByIdAndDeletedAtIsNull(request.getProjectId())
                    .orElseThrow(() -> new ResourceNotFoundException("Active project not found"));
            requireProjectOwnerOrAdmin(project, currentUser);

            task.setProject(project);
        }

        if (request.getAssigneeId() != null) {
            User assignee = findActiveUser(request.getAssigneeId());
            requireAssigneeAllowed(task, assignee, currentUser);
            task.setAssignee(assignee);
        } else if (task.getProject() == null) {
            task.setAssignee(currentUser);
        }

        Task savedTask = taskRepository.save(task);
        return taskMapper.toResponse(savedTask);
    }

    @Override
    public Page<TaskResponse> getTasks(FilterTaskRequest request, Pageable pageable) {
        User currentUser = currentUserService.getCurrentUser();
        Page<Task> tasks = taskRepository.findAll(
                currentUserService.isAdmin(currentUser)
                        ? TaskSpecification.filter(request)
                        : TaskSpecification.visibleToUser(request, currentUser.getId()),
                pageable
        );

        return tasks.map(taskMapper::toResponse);
    }

    @Override
    public Page<TaskResponse> getDeletedTasks(Pageable pageable) {
        User currentUser = currentUserService.getCurrentUser();
        return taskRepository.findAll(
                        currentUserService.isAdmin(currentUser)
                                ? TaskSpecification.deleted()
                                : TaskSpecification.deletedVisibleToUser(currentUser.getId()),
                        pageable)
                .map(taskMapper::toResponse);

    }

    @Override
    public TaskResponse getTaskById(Long id) {
        Task task = taskRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task Not Found with id: " + id));
        requireTaskAccess(task, currentUserService.getCurrentUser());

        return taskMapper.toResponse(task);
    }

    @Transactional
    @Override
    public TaskResponse updateTask(Long id, UpdateTaskRequest request) {
        Task currentTask = taskRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
        User currentUser = currentUserService.getCurrentUser();
        requireTaskAccess(currentTask, currentUser);

        if (request.getProjectId() != null) {
            Project project = projectRepository.findByIdAndDeletedAtIsNull(request.getProjectId())
                    .orElseThrow(() -> new ResourceNotFoundException("Active project not found"));
            requireProjectOwnerOrAdmin(project, currentUser);

            currentTask.setProject(project);
        }

        if (request.getAssigneeId() != null) {
            User assignee = findActiveUser(request.getAssigneeId());
            requireAssigneeAllowed(currentTask, assignee, currentUser);
            currentTask.setAssignee(assignee);
        }

        taskMapper.updateEntity(request, currentTask);
        Task updatedTask = taskRepository.save(currentTask);
        return taskMapper.toResponse(updatedTask);
    }

    @Override
    @Transactional
    public TaskResponse partialUpdateTask(Long id, PartialUpdateTaskRequest request) {
        Task currentTask = taskRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id:" + id));
        User currentUser = currentUserService.getCurrentUser();
        requireTaskAccess(currentTask, currentUser);

        if (request.getProjectId() != null) {
            Project project = projectRepository.findByIdAndDeletedAtIsNull(request.getProjectId())
                    .orElseThrow(() -> new ResourceNotFoundException("Active project not found"));
            requireProjectOwnerOrAdmin(project, currentUser);

            currentTask.setProject(project);
        }
        if (request.getAssigneeId() != null) {
            User assignee = findActiveUser(request.getAssigneeId());
            requireAssigneeAllowed(currentTask, assignee, currentUser);
            currentTask.setAssignee(assignee);
        }
        taskMapper.partialUpdateEntity(request, currentTask);
        Task updatedTask = taskRepository.save((currentTask));
        return taskMapper.toResponse(updatedTask);
    }

    @Transactional
    @Override
    public void deleteTask(Long id) {
        Task task = taskRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task Not Found with id: " + id));
        requireTaskAccess(task, currentUserService.getCurrentUser());

        task.markAsDeleted();
    }

    @Transactional
    @Override
    public void hardDeleteTask(Long id) {
        requireAdmin(currentUserService.getCurrentUser());
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task Not Found with id: " + id));

        taskRepository.delete(task);
    }

    @Transactional
    @Override
    public TaskResponse restoreTask(Long id) {
        Task task = taskRepository.findByIdAndDeletedAtIsNotNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Deleted Task Not Found with id: " + id));
        requireTaskAccess(task, currentUserService.getCurrentUser());

        if (task.getProject() != null && task.getProject().getDeletedAt() != null) {
            throw new BadRequestException("Cannot restore task because its project is deleted");
        }

        task.restore();
        return taskMapper.toResponse(task);
    }

    @Transactional
    @Override
    public TaskResponse changeTaskStatus(Long id, ChangeTaskStatusRequest request) {
        Task task = taskRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
        requireTaskAccess(task, currentUserService.getCurrentUser());

        taskMapper.changeTaskStatus(request, task);
        Task updatedTask = taskRepository.save(task);
        return taskMapper.toResponse(updatedTask);
    }

    @Transactional
    @Override
    public TaskResponse changeTaskPriority(Long id, ChangeTaskPriorityRequest request) {
        Task task = taskRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
        requireTaskAccess(task, currentUserService.getCurrentUser());

        taskMapper.changeTaskPriority(request, task);
        Task updatedTask = taskRepository.save(task);
        return taskMapper.toResponse(updatedTask);
    }

    @Override
    public Page<TaskResponse> getTasksByProjectId(Long projectId, FilterTaskRequest request, Pageable pageable) {
        Project project = projectRepository.findByIdAndDeletedAtIsNull(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Active project not found"));
        requireProjectOwnerOrAdmin(project, currentUserService.getCurrentUser());

        if (request == null) {
            request = new FilterTaskRequest();
        }

        request.setProjectId(projectId);

        return taskRepository.findAll(TaskSpecification.filter(request), pageable)
                .map(taskMapper::toResponse);
    }

    private User findActiveUser(Long userId) {
        return userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Active user not found"));
    }

    private void requireTaskAccess(Task task, User currentUser) {
        if (currentUserService.isAdmin(currentUser)) {
            return;
        }
        if (isTaskAccessibleTo(task, currentUser)) {
            return;
        }
        throw new ForbiddenException("You can only access tasks assigned to you or owned through your projects");
    }

    private boolean isTaskAccessibleTo(Task task, User currentUser) {
        return isAssignee(task, currentUser) || isProjectOwner(task.getProject(), currentUser);
    }

    private boolean isAssignee(Task task, User currentUser) {
        return task.getAssignee() != null && task.getAssignee().getId().equals(currentUser.getId());
    }

    private boolean isProjectOwner(Project project, User currentUser) {
        return project != null && project.getOwner() != null && project.getOwner().getId().equals(currentUser.getId());
    }

    private void requireProjectOwnerOrAdmin(Project project, User currentUser) {
        if (currentUserService.isAdmin(currentUser) || isProjectOwner(project, currentUser)) {
            return;
        }
        throw new ForbiddenException("You can only access your own projects");
    }

    private void requireAssigneeAllowed(Task task, User assignee, User currentUser) {
        if (currentUserService.isAdmin(currentUser) || isProjectOwner(task.getProject(), currentUser)) {
            return;
        }
        if (!assignee.getId().equals(currentUser.getId())) {
            throw new ForbiddenException("You can only assign standalone tasks to yourself");
        }
    }

    private void requireAdmin(User currentUser) {
        if (!currentUserService.isAdmin(currentUser)) {
            throw new ForbiddenException("Admin access is required");
        }
    }

}
