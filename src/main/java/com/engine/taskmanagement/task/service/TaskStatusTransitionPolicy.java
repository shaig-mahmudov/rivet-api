package com.engine.taskmanagement.task.service;

import com.engine.taskmanagement.task.enums.TaskStatus;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

@Component
public class TaskStatusTransitionPolicy {

    private static final Map<TaskStatus, Set<TaskStatus>> ALLOWED_TRANSITIONS = new EnumMap<>(TaskStatus.class);
    private static final Set<String> REASON_REQUIRED_TRANSITIONS = Set.of(
            key(TaskStatus.IN_PROGRESS, TaskStatus.BLOCKED),
            key(TaskStatus.IN_REVIEW, TaskStatus.IN_PROGRESS),
            key(TaskStatus.IN_REVIEW, TaskStatus.BLOCKED),
            key(TaskStatus.DONE, TaskStatus.REOPENED)
    );

    static {
        ALLOWED_TRANSITIONS.put(TaskStatus.TODO, EnumSet.of(TaskStatus.IN_PROGRESS, TaskStatus.CANCELLED));
        ALLOWED_TRANSITIONS.put(TaskStatus.IN_PROGRESS, EnumSet.of(TaskStatus.IN_REVIEW, TaskStatus.BLOCKED, TaskStatus.CANCELLED));
        ALLOWED_TRANSITIONS.put(TaskStatus.BLOCKED, EnumSet.of(TaskStatus.IN_PROGRESS, TaskStatus.CANCELLED));
        ALLOWED_TRANSITIONS.put(TaskStatus.IN_REVIEW, EnumSet.of(TaskStatus.DONE, TaskStatus.IN_PROGRESS, TaskStatus.BLOCKED));
        ALLOWED_TRANSITIONS.put(TaskStatus.DONE, EnumSet.of(TaskStatus.REOPENED));
        ALLOWED_TRANSITIONS.put(TaskStatus.REOPENED, EnumSet.of(TaskStatus.IN_PROGRESS, TaskStatus.CANCELLED));
    }

    public boolean canTransition(TaskStatus from, TaskStatus to) {
        if (from == null || to == null) {
            return false;
        }
        return ALLOWED_TRANSITIONS.getOrDefault(from, Set.of()).contains(to);
    }

    public boolean requiresReason(TaskStatus from, TaskStatus to) {
        if (from == null || to == null) {
            return false;
        }
        return REASON_REQUIRED_TRANSITIONS.contains(key(from, to));
    }

    private static String key(TaskStatus from, TaskStatus to) {
        return from.name() + "->" + to.name();
    }
}
