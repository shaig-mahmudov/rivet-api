package com.engine.taskmanagement.task.controller;

import com.engine.taskmanagement.task.dto.request.CreateTaskRequest;
import com.engine.taskmanagement.task.dto.request.PartialUpdateTaskRequest;
import com.engine.taskmanagement.task.dto.request.UpdateTaskRequest;
import com.engine.taskmanagement.task.dto.response.TaskResponse;
import com.engine.taskmanagement.task.service.abstraction.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseEntity<List<TaskResponse>> getAllTasks() {
        List<TaskResponse> response = taskService.getAllTasks();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/deleted")
    public ResponseEntity<List<TaskResponse>> getDeletedTasks() {
        List<TaskResponse> response = taskService.getDeletedTasks();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> getTaskById(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.getTaskById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/hard")
    public ResponseEntity<Void> hardDeleteTask(@PathVariable Long id) {
        taskService.hardDeleteTask(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskResponse> updateTask(
            @PathVariable Long id,
            @RequestBody UpdateTaskRequest request
    ) {
        TaskResponse response = taskService.updateTask(id, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("{id}/restore")
    public ResponseEntity<TaskResponse> restoreTask(@PathVariable Long id) {
        TaskResponse response = taskService.restoreTask(id);
        return ResponseEntity.ok(response);

    }

    @PatchMapping("/{id}")
    public ResponseEntity<TaskResponse> partialUpdateTask(@PathVariable Long id, @RequestBody PartialUpdateTaskRequest request) {
        TaskResponse response = taskService.partialUpdateTask(id, request);
        return ResponseEntity.ok(response);
    }
}
