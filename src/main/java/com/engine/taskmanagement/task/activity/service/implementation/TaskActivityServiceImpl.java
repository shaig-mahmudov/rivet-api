package com.engine.taskmanagement.task.activity.service.implementation;

import com.engine.taskmanagement.auth.service.CurrentUserService;
import com.engine.taskmanagement.common.exception.ForbiddenException;
import com.engine.taskmanagement.common.exception.ResourceNotFoundException;
import com.engine.taskmanagement.project.entity.Project;
import com.engine.taskmanagement.task.activity.dto.response.TaskActivityActorResponse;
import com.engine.taskmanagement.task.activity.dto.response.TaskActivityResponse;
import com.engine.taskmanagement.task.activity.entity.TaskActivity;
import com.engine.taskmanagement.task.activity.enums.TaskActivityType;
import com.engine.taskmanagement.task.activity.repository.TaskActivityRepository;
import com.engine.taskmanagement.task.activity.service.abstraction.TaskActivityService;
import com.engine.taskmanagement.task.entity.Task;
import com.engine.taskmanagement.task.enums.Severity;
import com.engine.taskmanagement.task.enums.TaskPriority;
import com.engine.taskmanagement.task.enums.TaskStatus;
import com.engine.taskmanagement.task.enums.TaskType;
import com.engine.taskmanagement.task.repository.TaskRepository;
import com.engine.taskmanagement.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
public class TaskActivityServiceImpl implements TaskActivityService {

    private final TaskActivityRepository taskActivityRepository;
    private final TaskRepository taskRepository;
    private final CurrentUserService currentUserService;

    public TaskActivityServiceImpl(
            TaskActivityRepository taskActivityRepository,
            TaskRepository taskRepository,
            CurrentUserService currentUserService
    ) {
        this.taskActivityRepository = taskActivityRepository;
        this.taskRepository = taskRepository;
        this.currentUserService = currentUserService;
    }

    @Override
    @Transactional
    public void recordTaskCreated(Task task, User actor) {
        record(task, actor, TaskActivityType.TASK_CREATED, null, task.getStatus().name(), "Task created", null);
    }

    @Override
    @Transactional
    public void recordStatusChanged(Task task, User actor, TaskStatus oldStatus, TaskStatus newStatus, String reason) {
        if (Objects.equals(oldStatus, newStatus)) {
            return;
        }
        TaskActivityType activityType = statusActivityType(newStatus);
        String message = "Status changed from " + oldStatus + " to " + newStatus;
        String metadata = null;

        if (TaskStatus.BLOCKED.equals(newStatus) && reason != null) {
            message = "Task blocked: " + reason;
            metadata = "reason=" + reason;
        }

        record(task, actor, activityType, valueOf(oldStatus), valueOf(newStatus), message, metadata);
    }

    @Override
    @Transactional
    public void recordPriorityChanged(Task task, User actor, TaskPriority oldPriority, TaskPriority newPriority) {
        if (Objects.equals(oldPriority, newPriority)) {
            return;
        }
        record(
                task,
                actor,
                TaskActivityType.PRIORITY_CHANGED,
                valueOf(oldPriority),
                valueOf(newPriority),
                "Priority changed from " + oldPriority + " to " + newPriority,
                null
        );
    }

    @Override
    @Transactional
    public void recordAssigneeChanged(Task task, User actor, User oldAssignee, User newAssignee) {
        if (sameUser(oldAssignee, newAssignee)) {
            return;
        }
        record(
                task,
                actor,
                TaskActivityType.ASSIGNEE_CHANGED,
                oldAssignee == null ? null : oldAssignee.getId().toString(),
                newAssignee == null ? null : newAssignee.getId().toString(),
                "Assignee changed from " + userName(oldAssignee) + " to " + userName(newAssignee),
                null
        );
    }

    @Override
    @Transactional
    public void recordTypeChanged(Task task, User actor, TaskType oldType, TaskType newType) {
        if (Objects.equals(oldType, newType)) {
            return;
        }
        record(
                task,
                actor,
                TaskActivityType.TYPE_CHANGED,
                valueOf(oldType),
                valueOf(newType),
                "Type changed from " + oldType + " to " + newType,
                null
        );
    }

    @Override
    @Transactional
    public void recordSeverityChanged(Task task, User actor, Severity oldSeverity, Severity newSeverity) {
        if (Objects.equals(oldSeverity, newSeverity)) {
            return;
        }
        record(
                task,
                actor,
                TaskActivityType.SEVERITY_CHANGED,
                valueOf(oldSeverity),
                valueOf(newSeverity),
                "Severity changed from " + oldSeverity + " to " + newSeverity,
                null
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TaskActivityResponse> getTaskTimeline(Long taskId, Pageable pageable) {
        Task task = taskRepository.findByIdAndDeletedAtIsNull(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));
        requireTaskAccess(task, currentUserService.getCurrentUser());

        return taskActivityRepository.findByTaskId(taskId, pageable)
                .map(this::toResponse);
    }

    private void record(
            Task task,
            User actor,
            TaskActivityType type,
            String oldValue,
            String newValue,
            String message,
            String metadata
    ) {
        TaskActivity activity = new TaskActivity();
        activity.setTask(task);
        activity.setActor(actor);
        activity.setType(type);
        activity.setOldValue(oldValue);
        activity.setNewValue(newValue);
        activity.setMessage(message);
        activity.setMetadata(metadata);
        taskActivityRepository.save(activity);
    }

    private TaskActivityResponse toResponse(TaskActivity activity) {
        TaskActivityResponse response = new TaskActivityResponse();
        response.setId(activity.getId());
        response.setType(activity.getType());
        response.setActor(toActorResponse(activity.getActor()));
        response.setOldValue(activity.getOldValue());
        response.setNewValue(activity.getNewValue());
        response.setMessage(activity.getMessage());
        response.setMetadata(activity.getMetadata());
        response.setCreatedAt(activity.getCreatedAt());
        return response;
    }

    private TaskActivityActorResponse toActorResponse(User actor) {
        if (actor == null) {
            return null;
        }
        TaskActivityActorResponse response = new TaskActivityActorResponse();
        response.setId(actor.getId());
        response.setName(userName(actor));
        return response;
    }

    private TaskActivityType statusActivityType(TaskStatus newStatus) {
        if (TaskStatus.BLOCKED.equals(newStatus)) {
            return TaskActivityType.TASK_BLOCKED;
        }
        if (TaskStatus.REOPENED.equals(newStatus)) {
            return TaskActivityType.TASK_REOPENED;
        }
        if (TaskStatus.CANCELLED.equals(newStatus)) {
            return TaskActivityType.TASK_CANCELLED;
        }
        return TaskActivityType.STATUS_CHANGED;
    }

    private String valueOf(Enum<?> value) {
        return value == null ? null : value.name();
    }

    private boolean sameUser(User first, User second) {
        if (first == null || second == null) {
            return first == second;
        }
        return Objects.equals(first.getId(), second.getId());
    }

    private String userName(User user) {
        if (user == null) {
            return "unassigned";
        }
        if (user.getUsername() != null && !user.getUsername().isBlank()) {
            return user.getUsername();
        }
        return user.getEmail();
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
