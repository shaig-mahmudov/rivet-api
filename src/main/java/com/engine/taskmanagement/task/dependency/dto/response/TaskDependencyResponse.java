package com.engine.taskmanagement.task.dependency.dto.response;

import com.engine.taskmanagement.task.enums.TaskStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class TaskDependencyResponse {
    private Long id;
    private Long taskId;
    private Long dependsOnTaskId;
    private String dependsOnTaskTitle;
    private TaskStatus dependsOnTaskStatus;
    private Long createdById;
    private LocalDateTime createdAt;
}
