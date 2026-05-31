package com.engine.taskmanagement.task.dto.request;

import com.engine.taskmanagement.task.enums.TaskPriority;
import com.engine.taskmanagement.task.enums.TaskStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FilterTaskRequest {
    private TaskStatus status;
    private TaskPriority priority;
    private Long projectId;
}