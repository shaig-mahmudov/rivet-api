package com.engine.taskmanagement.task.comment.service.abstraction;

import com.engine.taskmanagement.task.comment.dto.request.CreateTaskCommentRequest;
import com.engine.taskmanagement.task.comment.dto.request.UpdateTaskCommentRequest;
import com.engine.taskmanagement.task.comment.dto.response.TaskCommentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TaskCommentService {

    TaskCommentResponse create(Long taskId, CreateTaskCommentRequest request);

    Page<TaskCommentResponse> list(Long taskId, Pageable pageable);

    TaskCommentResponse update(Long taskId, Long commentId, UpdateTaskCommentRequest request);

    void delete(Long taskId, Long commentId);
}
