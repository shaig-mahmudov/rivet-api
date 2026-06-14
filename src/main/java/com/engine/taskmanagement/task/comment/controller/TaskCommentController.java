package com.engine.taskmanagement.task.comment.controller;

import com.engine.taskmanagement.task.comment.dto.request.CreateTaskCommentRequest;
import com.engine.taskmanagement.task.comment.dto.request.UpdateTaskCommentRequest;
import com.engine.taskmanagement.task.comment.dto.response.TaskCommentResponse;
import com.engine.taskmanagement.task.comment.service.abstraction.TaskCommentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tasks/{taskId}/comments")
public class TaskCommentController {

    private final TaskCommentService taskCommentService;

    public TaskCommentController(TaskCommentService taskCommentService) {
        this.taskCommentService = taskCommentService;
    }

    @PostMapping
    public ResponseEntity<TaskCommentResponse> create(
            @PathVariable Long taskId,
            @Valid @RequestBody CreateTaskCommentRequest request
    ) {
        TaskCommentResponse response = taskCommentService.create(taskId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<TaskCommentResponse>> list(
            @PathVariable Long taskId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.ASC)
            Pageable pageable
    ) {
        return ResponseEntity.ok(taskCommentService.list(taskId, pageable));
    }

    @PatchMapping("/{commentId}")
    public ResponseEntity<TaskCommentResponse> update(
            @PathVariable Long taskId,
            @PathVariable Long commentId,
            @Valid @RequestBody UpdateTaskCommentRequest request
    ) {
        return ResponseEntity.ok(taskCommentService.update(taskId, commentId, request));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long taskId,
            @PathVariable Long commentId
    ) {
        taskCommentService.delete(taskId, commentId);
        return ResponseEntity.noContent().build();
    }
}
