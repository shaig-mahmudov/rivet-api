package com.engine.taskmanagement.task.dto.request;

import com.engine.taskmanagement.task.enums.Severity;
import com.engine.taskmanagement.task.enums.TaskPriority;
import com.engine.taskmanagement.task.enums.TaskStatus;
import com.engine.taskmanagement.task.enums.TaskType;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;


@Getter
@Setter
public class PartialUpdateTaskRequest {
    private Long projectId;
    private Long assigneeId;
    private String title;
    private String description;
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
