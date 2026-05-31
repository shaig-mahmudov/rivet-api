package com.engine.taskmanagement.task.service.abstraction;

import com.engine.taskmanagement.task.dto.request.*;
import com.engine.taskmanagement.task.dto.response.TaskResponse;

import java.util.List;

public interface TaskService {

    TaskResponse createTask(CreateTaskRequest request);
    List<TaskResponse> getAllTasks();
    List<TaskResponse> getDeletedTasks();
    TaskResponse getTaskById(Long id);
    TaskResponse updateTask(Long id, UpdateTaskRequest request);
    TaskResponse partialUpdateTask(Long id, PartialUpdateTaskRequest request);
    void deleteTask(Long id);
    void hardDeleteTask(Long id);
    TaskResponse restoreTask(Long id);
    TaskResponse changeTaskStatus(Long id, ChangeTaskStatusRequest request);
    TaskResponse changeTaskPriority(Long id, ChangeTaskPriorityRequest request);
    List<TaskResponse> getFilteredTasks(FilterTaskRequest request);
}
