package com.engine.taskmanagement.task.service.abstraction;

import com.engine.taskmanagement.task.dto.request.TaskTransitionRequest;
import com.engine.taskmanagement.task.dto.response.TaskTransitionResponse;
import com.engine.taskmanagement.task.entity.Task;
import com.engine.taskmanagement.task.enums.TaskStatus;
import com.engine.taskmanagement.user.entity.User;

public interface TaskStatusTransitionService {

    TaskTransitionResponse transitionTaskStatus(Long taskId, TaskTransitionRequest request);

    Task transitionTaskStatus(Task task, TaskStatus targetStatus, String reason, User currentUser);
}
