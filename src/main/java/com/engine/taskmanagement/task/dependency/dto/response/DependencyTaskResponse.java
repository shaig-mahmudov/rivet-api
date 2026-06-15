package com.engine.taskmanagement.task.dependency.dto.response;

import com.engine.taskmanagement.task.enums.TaskStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DependencyTaskResponse {
    private Long taskId;
    private String title;
    private TaskStatus status;
}
