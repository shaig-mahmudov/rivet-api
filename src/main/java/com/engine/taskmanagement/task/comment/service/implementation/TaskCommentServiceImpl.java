package com.engine.taskmanagement.task.comment.service.implementation;

import com.engine.taskmanagement.auth.service.CurrentUserService;
import com.engine.taskmanagement.common.exception.ForbiddenException;
import com.engine.taskmanagement.common.exception.ResourceNotFoundException;
import com.engine.taskmanagement.project.entity.Project;
import com.engine.taskmanagement.task.activity.service.abstraction.TaskActivityService;
import com.engine.taskmanagement.task.comment.dto.request.CreateTaskCommentRequest;
import com.engine.taskmanagement.task.comment.dto.request.UpdateTaskCommentRequest;
import com.engine.taskmanagement.task.comment.dto.response.TaskCommentResponse;
import com.engine.taskmanagement.task.comment.entity.TaskComment;
import com.engine.taskmanagement.task.comment.repository.TaskCommentRepository;
import com.engine.taskmanagement.task.comment.service.abstraction.TaskCommentService;
import com.engine.taskmanagement.task.entity.Task;
import com.engine.taskmanagement.task.repository.TaskRepository;
import com.engine.taskmanagement.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TaskCommentServiceImpl implements TaskCommentService {

    private final TaskCommentRepository taskCommentRepository;
    private final TaskRepository taskRepository;
    private final CurrentUserService currentUserService;
    private final TaskActivityService taskActivityService;

    public TaskCommentServiceImpl(
            TaskCommentRepository taskCommentRepository,
            TaskRepository taskRepository,
            CurrentUserService currentUserService,
            TaskActivityService taskActivityService
    ) {
        this.taskCommentRepository = taskCommentRepository;
        this.taskRepository = taskRepository;
        this.currentUserService = currentUserService;
        this.taskActivityService = taskActivityService;
    }

    @Override
    @Transactional
    public TaskCommentResponse create(Long taskId, CreateTaskCommentRequest request) {
        Task task = findTaskForCurrentUser(taskId);
        User currentUser = currentUserService.getCurrentUser();
        TaskComment comment = new TaskComment();
        comment.setTask(task);
        comment.setAuthor(currentUser);
        comment.setType(request.getType());
        comment.setBody(request.getBody().trim());

        TaskComment savedComment = taskCommentRepository.save(comment);
        taskActivityService.recordCommentAdded(task, currentUser, savedComment.getBody());
        return toResponse(savedComment);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TaskCommentResponse> list(Long taskId, Pageable pageable) {
        findTaskForCurrentUser(taskId);
        return taskCommentRepository.findByTaskIdAndDeletedAtIsNull(taskId, pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional
    public TaskCommentResponse update(Long taskId, Long commentId, UpdateTaskCommentRequest request) {
        Task task = findTaskForCurrentUser(taskId);
        User currentUser = currentUserService.getCurrentUser();
        TaskComment comment = findComment(taskId, commentId);
        requireCommentAuthorProjectOwnerOrAdmin(comment, currentUser);
        String oldBody = comment.getBody();
        comment.setBody(request.getBody().trim());

        TaskComment savedComment = taskCommentRepository.save(comment);
        taskActivityService.recordCommentUpdated(task, currentUser, oldBody, savedComment.getBody());
        return toResponse(savedComment);
    }

    @Override
    @Transactional
    public void delete(Long taskId, Long commentId) {
        Task task = findTaskForCurrentUser(taskId);
        User currentUser = currentUserService.getCurrentUser();
        TaskComment comment = findComment(taskId, commentId);
        requireProjectOwnerOrAdmin(comment.getTask(), currentUser);

        comment.markAsDeleted();
        taskActivityService.recordCommentDeleted(task, currentUser, comment.getBody());
    }

    private Task findTaskForCurrentUser(Long taskId) {
        Task task = taskRepository.findByIdAndDeletedAtIsNull(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));
        requireTaskAccess(task, currentUserService.getCurrentUser());
        return task;
    }

    private TaskComment findComment(Long taskId, Long commentId) {
        return taskCommentRepository.findByIdAndTaskIdAndDeletedAtIsNull(commentId, taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task comment not found with id: " + commentId));
    }

    private TaskCommentResponse toResponse(TaskComment comment) {
        TaskCommentResponse response = new TaskCommentResponse();
        response.setId(comment.getId());
        response.setTaskId(comment.getTask().getId());
        response.setAuthorId(comment.getAuthor().getId());
        response.setType(comment.getType());
        response.setBody(comment.getBody());
        response.setCreatedAt(comment.getCreatedAt());
        response.setUpdatedAt(comment.getUpdatedAt());
        return response;
    }

    private void requireCommentAuthorProjectOwnerOrAdmin(TaskComment comment, User currentUser) {
        if (currentUserService.isAdmin(currentUser) || isProjectOwner(comment.getTask().getProject(), currentUser)) {
            return;
        }
        if (comment.getAuthor().getId().equals(currentUser.getId())) {
            return;
        }
        throw new ForbiddenException("You can only update your own comments or comments on your projects");
    }

    private void requireProjectOwnerOrAdmin(Task task, User currentUser) {
        if (currentUserService.isAdmin(currentUser) || isProjectOwner(task.getProject(), currentUser)) {
            return;
        }
        throw new ForbiddenException("You can only delete comments on your projects");
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
