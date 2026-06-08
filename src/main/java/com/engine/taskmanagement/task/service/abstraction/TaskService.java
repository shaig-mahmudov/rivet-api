package com.engine.taskmanagement.task.service.abstraction;

import com.engine.taskmanagement.task.dto.request.*;
import com.engine.taskmanagement.task.dto.response.TaskResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TaskService {

    TaskResponse createTask(CreateTaskRequest request);
    Page<TaskResponse> getTasks(FilterTaskRequest request, Pageable pageable);
    Page<TaskResponse> getDeletedTasks(Pageable pageable);
    TaskResponse getTaskById(Long id);
    TaskResponse updateTask(Long id, UpdateTaskRequest request);
    TaskResponse partialUpdateTask(Long id, PartialUpdateTaskRequest request);
    void deleteTask(Long id);
    void hardDeleteTask(Long id);
    TaskResponse restoreTask(Long id);
    TaskResponse changeTaskStatus(Long id, ChangeTaskStatusRequest request);
    TaskResponse changeTaskPriority(Long id, ChangeTaskPriorityRequest request);
    Page<TaskResponse> getTasksByProjectId(Long projectId, FilterTaskRequest request, Pageable pageable);

}
