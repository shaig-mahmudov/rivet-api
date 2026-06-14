package com.engine.taskmanagement.task.criteria.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class BulkCreateAcceptanceCriteriaRequest {

    @NotEmpty(message = "At least one acceptance criteria item is required")
    @Size(max = 50, message = "Cannot create more than 50 acceptance criteria items at once")
    private List<@NotBlank(message = "Acceptance criteria text is required")
    @Size(max = 500, message = "Acceptance criteria text cannot exceed 500 characters") String> items;
}
