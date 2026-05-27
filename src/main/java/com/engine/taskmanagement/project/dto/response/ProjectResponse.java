package com.engine.taskmanagement.project.dto.response;

import com.engine.taskmanagement.task.entity.Task;
import com.engine.taskmanagement.user.entity.User;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ProjectResponse {
    private Long id;
    private String name;
    private String description;
    private User owner;
    private List<Task> tasks = new ArrayList<>();
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
}
