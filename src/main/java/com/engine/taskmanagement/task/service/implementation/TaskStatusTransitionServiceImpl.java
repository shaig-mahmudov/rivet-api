package com.engine.taskmanagement.task.service.implementation;

import com.engine.taskmanagement.auth.service.CurrentUserService;
import com.engine.taskmanagement.common.exception.BadRequestException;
import com.engine.taskmanagement.common.exception.ForbiddenException;
import com.engine.taskmanagement.common.exception.IncompleteTaskDependenciesException;
import com.engine.taskmanagement.common.exception.ResourceNotFoundException;
import com.engine.taskmanagement.project.entity.Project;
import com.engine.taskmanagement.task.dto.request.TaskTransitionRequest;
import com.engine.taskmanagement.task.dto.response.TaskTransitionResponse;
import com.engine.taskmanagement.task.dependency.dto.response.DependencyTaskResponse;
import com.engine.taskmanagement.task.dependency.entity.TaskDependency;
import com.engine.taskmanagement.task.dependency.repository.TaskDependencyRepository;
import com.engine.taskmanagement.task.entity.Task;
import com.engine.taskmanagement.task.enums.TaskStatus;
import com.engine.taskmanagement.task.repository.TaskRepository;
import com.engine.taskmanagement.task.activity.service.abstraction.TaskActivityService;
import com.engine.taskmanagement.task.criteria.repository.AcceptanceCriteriaRepository;
import com.engine.taskmanagement.task.service.TaskStatusTransitionPolicy;
import com.engine.taskmanagement.task.service.abstraction.TaskStatusTransitionService;
import com.engine.taskmanagement.user.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class TaskStatusTransitionServiceImpl implements TaskStatusTransitionService {

    private static final int MAX_REASON_LENGTH = 500;

    private final TaskRepository taskRepository;
    private final CurrentUserService currentUserService;
    private final TaskStatusTransitionPolicy transitionPolicy;
    private final TaskActivityService taskActivityService;
    private final AcceptanceCriteriaRepository acceptanceCriteriaRepository;
    private final TaskDependencyRepository taskDependencyRepository;

    public TaskStatusTransitionServiceImpl(
            TaskRepository taskRepository,
            CurrentUserService currentUserService,
            TaskStatusTransitionPolicy transitionPolicy,
            TaskActivityService taskActivityService,
            AcceptanceCriteriaRepository acceptanceCriteriaRepository,
            TaskDependencyRepository taskDependencyRepository
    ) {
        this.taskRepository = taskRepository;
        this.currentUserService = currentUserService;
        this.transitionPolicy = transitionPolicy;
        this.taskActivityService = taskActivityService;
        this.acceptanceCriteriaRepository = acceptanceCriteriaRepository;
        this.taskDependencyRepository = taskDependencyRepository;
    }

    @Override
    @Transactional
    public TaskTransitionResponse transitionTaskStatus(Long taskId, TaskTransitionRequest request) {
        Task task = taskRepository.findByIdAndDeletedAtIsNull(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));
        User currentUser = currentUserService.getCurrentUser();
        TaskStatus previousStatus = task.getStatus();
        String reason = normalizeReason(request.getReason());

        transitionTaskStatus(task, request.getTargetStatus(), reason, currentUser);

        TaskTransitionResponse response = new TaskTransitionResponse();
        response.setTaskId(task.getId());
        response.setPreviousStatus(previousStatus);
        response.setCurrentStatus(task.getStatus());
        response.setReason(reason);
        response.setTransitionedAt(Instant.now());
        return response;
    }

    @Override
    @Transactional
    public Task transitionTaskStatus(Task task, TaskStatus targetStatus, String reason, User currentUser) {
        requireTaskAccess(task, currentUser);
        String normalizedReason = normalizeReason(reason);
        validateTransition(task, targetStatus, normalizedReason);

        TaskStatus previousStatus = task.getStatus();
        task.setStatus(targetStatus);
        Task updatedTask = taskRepository.save(task);
        taskActivityService.recordStatusChanged(updatedTask, currentUser, previousStatus, targetStatus, normalizedReason);
        return updatedTask;
    }

    private void validateTransition(Task task, TaskStatus targetStatus, String reason) {
        TaskStatus currentStatus = task.getStatus();
        if (targetStatus == null) {
            throw new BadRequestException("Target status is required");
        }
        if (targetStatus.equals(currentStatus)) {
            throw new BadRequestException("Target status must be different from current status");
        }
        if (!transitionPolicy.canTransition(currentStatus, targetStatus)) {
            throw new BadRequestException("Invalid task status transition: " + currentStatus + " -> " + targetStatus);
        }
        if (transitionPolicy.requiresReason(currentStatus, targetStatus) && reason == null) {
            throw new BadRequestException("Reason is required for transition: " + currentStatus + " -> " + targetStatus);
        }
        if (reason != null && reason.length() > MAX_REASON_LENGTH) {
            throw new BadRequestException("Reason cannot exceed 500 characters");
        }
        if (TaskStatus.DONE.equals(targetStatus) && acceptanceCriteriaRepository.existsByTaskIdAndCompletedFalse(task.getId())) {
            throw new BadRequestException("Cannot complete task while acceptance criteria are incomplete");
        }
        if (TaskStatus.DONE.equals(targetStatus)) {
            validateDependenciesComplete(task);
        }
    }

    private void validateDependenciesComplete(Task task) {
        List<DependencyTaskResponse> incompleteDependencies = taskDependencyRepository
                .findByTaskIdAndDependsOnTaskStatusNot(task.getId(), TaskStatus.DONE).stream()
                .map(TaskDependency::getDependsOnTask)
                .map(this::toDependencyTaskResponse)
                .toList();

        if (!incompleteDependencies.isEmpty()) {
            throw new IncompleteTaskDependenciesException(incompleteDependencies);
        }
    }

    private DependencyTaskResponse toDependencyTaskResponse(Task task) {
        DependencyTaskResponse response = new DependencyTaskResponse();
        response.setTaskId(task.getId());
        response.setTitle(task.getTitle());
        response.setStatus(task.getStatus());
        return response;
    }

    private String normalizeReason(String reason) {
        if (reason == null || reason.isBlank()) {
            return null;
        }
        return reason.trim();
    }

    private void requireTaskAccess(Task task, User currentUser) {
        if (currentUserService.isAdmin(currentUser)) {
            return;
        }
        if (isAssignee(task, currentUser) || isProjectOwner(task.getProject(), currentUser)) {
            return;
        }
        throw new ForbiddenException("You can only access tasks assigned to you or owned through your projects");
    }

    private boolean isAssignee(Task task, User currentUser) {
        return task.getAssignee() != null && task.getAssignee().getId().equals(currentUser.getId());
    }

    private boolean isProjectOwner(Project project, User currentUser) {
        return project != null && project.getOwner() != null && project.getOwner().getId().equals(currentUser.getId());
    }
}
