package com.engine.taskmanagement.task.comment.dto.response;

import com.engine.taskmanagement.task.comment.enums.TaskCommentType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class TaskCommentResponse {
    private Long id;
    private Long taskId;
    private Long authorId;
    private TaskCommentType type;
    private String body;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
