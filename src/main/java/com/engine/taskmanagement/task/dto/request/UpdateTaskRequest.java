package com.engine.taskmanagement.task.dto.request;

import com.engine.taskmanagement.task.enums.TaskPriority;
import com.engine.taskmanagement.task.enums.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class UpdateTaskRequest {

    private Long projectId;

    @NotBlank(message = "Title is required")
    @Size(max = 100, message = "Title cannot exceed 100 characters")
    private String title;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    @NotNull
    private TaskPriority priority;
    @NotNull
    private TaskStatus status;

    private LocalDate dueDate;
}