package com.engine.taskmanagement.project.dto.request;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UpdateProjectRequest {
    private String name;
    private String description;
}
