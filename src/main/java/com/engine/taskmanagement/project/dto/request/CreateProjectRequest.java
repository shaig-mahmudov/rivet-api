package com.engine.taskmanagement.project.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CreateProjectRequest {
    @NotBlank(message = "Project Name is required")
    @Size(max = 100, message = "Project Name cannot exceed 100 characters")
    private String name;

    @Size(max = 250, message = "Project Description cannot exceed 250 characters")
    private String description;
}
