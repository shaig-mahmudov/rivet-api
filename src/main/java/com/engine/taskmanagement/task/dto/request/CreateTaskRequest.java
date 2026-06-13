package com.engine.taskmanagement.task.dto.request;

import com.engine.taskmanagement.task.enums.Severity;
import com.engine.taskmanagement.task.enums.TaskPriority;
import com.engine.taskmanagement.task.enums.TaskStatus;
import com.engine.taskmanagement.task.enums.TaskType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;



@Getter
@Setter
public class CreateTaskRequest {

    private Long projectId;
    private Long assigneeId;

    @NotBlank(message = "Title is required")
    @Size(max = 100, message = "Title cannot exceed 100 characters")
    private String title;

    @Size(max = 250, message = "Description cannot exceed 250 characters")
    private String description;

    @NotNull(message = "Task type is required")
    private TaskType type;

    private Severity severity;

    @Size(max = 1000, message = "Technical Context cannot exceed 1000 characters")
    private String technicalContext;

    @Size(max = 1000, message = "Expected Outcome cannot exceed 1000 characters")
    private String expectedOutcome;

    private TaskPriority priority;
    private TaskStatus status;
    private LocalDate dueDate;
}
