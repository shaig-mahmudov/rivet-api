package com.engine.taskmanagement.task.dto.request;

import com.engine.taskmanagement.task.enums.TaskPriority;
import com.engine.taskmanagement.task.enums.TaskStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class FilterTaskRequest {
    private String search;
    private TaskStatus status;
    private TaskPriority priority;
    private Boolean dueFromToday;
    private LocalDate dueDateFrom;
    private LocalDate dueDateTo;
    private Long projectId;
}