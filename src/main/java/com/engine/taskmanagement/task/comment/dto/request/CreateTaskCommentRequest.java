package com.engine.taskmanagement.task.comment.dto.request;

import com.engine.taskmanagement.task.comment.enums.TaskCommentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateTaskCommentRequest {

    @NotNull(message = "Comment type is required")
    private TaskCommentType type;

    @NotBlank(message = "Comment body is required")
    @Size(max = 2000, message = "Comment body cannot exceed 2000 characters")
    private String body;
}
