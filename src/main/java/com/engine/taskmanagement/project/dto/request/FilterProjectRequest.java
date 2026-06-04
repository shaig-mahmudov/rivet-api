package com.engine.taskmanagement.project.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FilterProjectRequest {

    private String search;
    private Long ownerId;

}
