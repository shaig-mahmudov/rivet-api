package com.engine.taskmanagement.task.mapper;

import com.engine.taskmanagement.task.dto.request.*;
import com.engine.taskmanagement.task.dto.response.TaskResponse;
import com.engine.taskmanagement.task.entity.Task;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class TaskMapper {

    public Task toEntity(CreateTaskRequest request) {
        Task task = new Task();

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        if (request.getPriority() != null) {
            task.setPriority(request.getPriority());
        }
        if (request.getStatus() != null) {
            task.setStatus(request.getStatus());
        }
        task.setDueDate(request.getDueDate());
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());

        return task;
    }

    public TaskResponse toResponse(Task task) {
        TaskResponse response = new TaskResponse();

        response.setId(task.getId());
        response.setTitle(task.getTitle());
        response.setDescription(task.getDescription());
        response.setStatus(task.getStatus());
        response.setPriority(task.getPriority());
        response.setDueDate(task.getDueDate());
        response.setCreatedAt(task.getCreatedAt());
        response.setUpdatedAt(task.getUpdatedAt());

        if (task.getProject() != null) {
            response.setProjectId(task.getProject().getId());
        }

        return response;
    }

    public void updateEntity(Task task, UpdateTaskRequest request) {
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setStatus(request.getStatus());
        task.setPriority(request.getPriority());
        task.setDueDate(request.getDueDate());
        task.setUpdatedAt(LocalDateTime.now());
    }

    public void partialUpdateEntity(Task task, PartialUpdateTaskRequest request) {
        if (request.getTitle() != null) {
            task.setTitle(request.getTitle());
        }

        if (request.getDescription() != null) {
            task.setDescription(request.getDescription());
        }

        if (request.getPriority() != null) {
            task.setPriority(request.getPriority());
        }

        if (request.getStatus() != null) {
            task.setStatus(request.getStatus());
        }

        if (request.getDueDate() != null) {
            task.setDueDate(request.getDueDate());
        }

        task.setUpdatedAt(LocalDateTime.now());
    }

    public void changeTaskStatus(Task task, ChangeTaskStatusRequest request) {
        task.setStatus(request.getStatus());
        task.setUpdatedAt(LocalDateTime.now());
    }

    public void changeTaskPriority(Task task, ChangeTaskPriorityRequest request) {
        task.setPriority(request.getPriority());
        task.setUpdatedAt(LocalDateTime.now());
    }
}
