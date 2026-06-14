package com.engine.taskmanagement.task.dto.response;

import com.engine.taskmanagement.task.enums.TaskStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class TaskTransitionResponse {
    private Long taskId;
    private TaskStatus previousStatus;
    private TaskStatus currentStatus;
    private String reason;
    private Instant transitionedAt;
}
