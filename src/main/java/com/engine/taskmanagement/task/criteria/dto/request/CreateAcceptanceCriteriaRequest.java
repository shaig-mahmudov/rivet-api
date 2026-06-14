package com.engine.taskmanagement.task.criteria.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateAcceptanceCriteriaRequest {

    @NotBlank(message = "Acceptance criteria text is required")
    @Size(max = 500, message = "Acceptance criteria text cannot exceed 500 characters")
    private String text;
}
