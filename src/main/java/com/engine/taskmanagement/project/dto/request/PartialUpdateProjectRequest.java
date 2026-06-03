package com.engine.taskmanagement.project.dto.request;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PartialUpdateProjectRequest {
    private String name;
    private String description;
}
