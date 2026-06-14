package com.engine.taskmanagement.task.activity.service.abstraction;

import com.engine.taskmanagement.task.activity.dto.response.TaskActivityResponse;
import com.engine.taskmanagement.task.entity.Task;
import com.engine.taskmanagement.task.enums.Severity;
import com.engine.taskmanagement.task.enums.TaskPriority;
import com.engine.taskmanagement.task.enums.TaskStatus;
import com.engine.taskmanagement.task.enums.TaskType;
import com.engine.taskmanagement.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TaskActivityService {

    void recordTaskCreated(Task task, User actor);

    void recordStatusChanged(Task task, User actor, TaskStatus oldStatus, TaskStatus newStatus, String reason);

    void recordPriorityChanged(Task task, User actor, TaskPriority oldPriority, TaskPriority newPriority);

    void recordAssigneeChanged(Task task, User actor, User oldAssignee, User newAssignee);

    void recordTypeChanged(Task task, User actor, TaskType oldType, TaskType newType);

    void recordSeverityChanged(Task task, User actor, Severity oldSeverity, Severity newSeverity);

    Page<TaskActivityResponse> getTaskTimeline(Long taskId, Pageable pageable);
}
