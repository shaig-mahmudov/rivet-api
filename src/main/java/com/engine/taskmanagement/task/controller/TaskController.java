package com.engine.taskmanagement.task.controller;

import com.engine.taskmanagement.task.dto.request.*;
import com.engine.taskmanagement.task.dto.response.TaskResponse;
import com.engine.taskmanagement.task.service.abstraction.TaskService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;


    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    public ResponseEntity<TaskResponse> createTask(
            @Valid @RequestBody CreateTaskRequest request
    ) {
        TaskResponse response = taskService.createTask(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<TaskResponse>> getTasks(
            @ModelAttribute FilterTaskRequest request,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        Page<TaskResponse> response = taskService.getTasks(request, pageable);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> getTaskById(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.getTaskById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskResponse> updateTask(
            @PathVariable Long id,
            @RequestBody @Valid UpdateTaskRequest request
    ) {
        TaskResponse response = taskService.updateTask(id, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<TaskResponse> partialUpdateTask(
            @PathVariable Long id,
            @Valid @RequestBody PartialUpdateTaskRequest request) {

        TaskResponse response = taskService.partialUpdateTask(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/deleted")
    public ResponseEntity<Page<TaskResponse>> getDeletedTasks(
            @PageableDefault(size = 10, sort = "deletedAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        Page<TaskResponse> response = taskService.getDeletedTasks(pageable);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}/hard")
    public ResponseEntity<Void> hardDeleteTask(@PathVariable Long id) {
        taskService.hardDeleteTask(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/restore")
    public ResponseEntity<TaskResponse> restoreTask(@PathVariable Long id) {
        TaskResponse response = taskService.restoreTask(id);
        return ResponseEntity.ok(response);

    }

    @PostMapping("/{id}/status")
    public ResponseEntity<TaskResponse> changeStatus(
            @PathVariable Long id,
            @Valid @RequestBody ChangeTaskStatusRequest request) {
        TaskResponse response = taskService.changeTaskStatus(id, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/priority")
    public ResponseEntity<TaskResponse> changePriority(
            @PathVariable Long id,
            @Valid @RequestBody ChangeTaskPriorityRequest request) {
        TaskResponse response = taskService.changeTaskPriority(id, request);
        return ResponseEntity.ok(response);

    }
}
