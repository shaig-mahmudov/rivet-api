package com.engine.taskmanagement.task.activity.dto.response;

import com.engine.taskmanagement.task.activity.enums.TaskActivityType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class TaskActivityResponse {
    private Long id;
    private TaskActivityType type;
    private TaskActivityActorResponse actor;
    private String oldValue;
    private String newValue;
    private String message;
    private String metadata;
    private LocalDateTime createdAt;
}
