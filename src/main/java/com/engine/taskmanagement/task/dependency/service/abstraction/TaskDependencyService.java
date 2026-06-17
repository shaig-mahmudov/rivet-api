package com.engine.taskmanagement.task.dependency.service.abstraction;

import com.engine.taskmanagement.task.dependency.dto.response.TaskDependencyResponse;
import com.engine.taskmanagement.task.dto.response.TaskResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TaskDependencyService {

    TaskDependencyResponse addDependency(Long taskId, Long dependsOnTaskId);

    void removeDependency(Long taskId, Long dependsOnTaskId);

    List<TaskDependencyResponse> listDependencies(Long taskId);

    List<TaskResponse> listBlockedTasks(Long taskId);

    List<TaskResponse> listBlockedTasks();

    Page<TaskResponse> listBlockedTasks(Pageable pageable);
}
