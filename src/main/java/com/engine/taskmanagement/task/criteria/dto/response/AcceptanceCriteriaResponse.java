package com.engine.taskmanagement.task.criteria.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class AcceptanceCriteriaResponse {
    private Long id;
    private Long taskId;
    private String text;
    private boolean completed;
    private Long createdById;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime completedAt;
    private Long completedById;
}
