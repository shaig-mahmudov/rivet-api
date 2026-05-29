package com.engine.taskmanagement.task.dto.request;

import com.engine.taskmanagement.task.enums.TaskPriority;
import com.engine.taskmanagement.task.enums.TaskStatus;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;


@Getter
@Setter
public class PartialUpdateTaskRequest {
    private String title;
    private String description;
    private TaskPriority priority;
    private TaskStatus status;
    private LocalDate dueDate;
}
