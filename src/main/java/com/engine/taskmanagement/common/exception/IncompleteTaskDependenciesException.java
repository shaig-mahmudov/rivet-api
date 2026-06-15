package com.engine.taskmanagement.common.exception;

import com.engine.taskmanagement.task.dependency.dto.response.DependencyTaskResponse;
import lombok.Getter;

import java.util.List;

@Getter
public class IncompleteTaskDependenciesException extends RuntimeException {

    private final List<DependencyTaskResponse> dependencies;

    public IncompleteTaskDependenciesException(List<DependencyTaskResponse> dependencies) {
        super("Task cannot be completed because it depends on unfinished tasks.");
        this.dependencies = dependencies;
    }
}
