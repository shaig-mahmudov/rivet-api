package com.engine.taskmanagement.task.activity.controller;

import com.engine.taskmanagement.task.activity.dto.response.TaskActivityResponse;
import com.engine.taskmanagement.task.activity.service.abstraction.TaskActivityService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tasks/{taskId}/timeline")
public class TaskActivityController {

    private final TaskActivityService taskActivityService;

    public TaskActivityController(TaskActivityService taskActivityService) {
        this.taskActivityService = taskActivityService;
    }

    @GetMapping
    public ResponseEntity<Page<TaskActivityResponse>> getTaskTimeline(
            @PathVariable Long taskId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        Page<TaskActivityResponse> response = taskActivityService.getTaskTimeline(taskId, pageable);
        return ResponseEntity.ok(response);
    }
}
