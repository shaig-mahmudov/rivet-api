package com.engine.taskmanagement.task.dto.request;

import com.engine.taskmanagement.task.enums.TaskStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskTransitionRequest {

    @NotNull(message = "Target status is required")
    private TaskStatus targetStatus;

    @Size(max = 500, message = "Reason cannot exceed 500 characters")
    private String reason;
}
