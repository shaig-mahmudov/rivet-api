package com.engine.taskmanagement.task.dependency.controller;

import com.engine.taskmanagement.task.dependency.dto.response.TaskDependencyResponse;
import com.engine.taskmanagement.task.dependency.service.abstraction.TaskDependencyService;
import com.engine.taskmanagement.task.dto.response.TaskResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskDependencyController {

    private final TaskDependencyService taskDependencyService;

    public TaskDependencyController(TaskDependencyService taskDependencyService) {
        this.taskDependencyService = taskDependencyService;
    }

    @GetMapping("/blocked")
    public ResponseEntity<List<TaskResponse>> listBlockedTasks() {
        return ResponseEntity.ok(taskDependencyService.listBlockedTasks());
    }

    @GetMapping("/blocked/page")
    public ResponseEntity<Page<TaskResponse>> listBlockedTasks(Pageable pageable) {
        return ResponseEntity.ok(taskDependencyService.listBlockedTasks(pageable));
    }

    @PostMapping("/{taskId}/dependencies/{dependsOnTaskId}")
    public ResponseEntity<TaskDependencyResponse> addDependency(
            @PathVariable Long taskId,
            @PathVariable Long dependsOnTaskId
    ) {
        TaskDependencyResponse response = taskDependencyService.addDependency(taskId, dependsOnTaskId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{taskId}/dependencies/{dependsOnTaskId}")
    public ResponseEntity<Void> removeDependency(
            @PathVariable Long taskId,
            @PathVariable Long dependsOnTaskId
    ) {
        taskDependencyService.removeDependency(taskId, dependsOnTaskId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{taskId}/dependencies")
    public ResponseEntity<List<TaskDependencyResponse>> listDependencies(@PathVariable Long taskId) {
        return ResponseEntity.ok(taskDependencyService.listDependencies(taskId));
    }

    @GetMapping("/{taskId}/blocked-tasks")
    public ResponseEntity<List<TaskResponse>> listBlockedTasks(@PathVariable Long taskId) {
        return ResponseEntity.ok(taskDependencyService.listBlockedTasks(taskId));
    }
}
