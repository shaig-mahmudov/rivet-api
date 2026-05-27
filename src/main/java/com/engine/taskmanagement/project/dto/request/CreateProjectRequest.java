package com.engine.taskmanagement.project.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CreateProjectRequest {
    @NotBlank(message = "Name is required")
    private String name;
    private String description;
}
