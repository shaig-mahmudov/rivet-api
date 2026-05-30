package com.engine.taskmanagement.task.dto.request;

import com.engine.taskmanagement.task.enums.TaskPriority;
import com.engine.taskmanagement.task.enums.TaskStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangeTaskStatusRequest {
    @NotNull(message = "Task status is required")
    private TaskStatus status;
}
