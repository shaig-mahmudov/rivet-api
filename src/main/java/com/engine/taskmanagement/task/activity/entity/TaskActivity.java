package com.engine.taskmanagement.task.activity.entity;

import com.engine.taskmanagement.common.entity.BaseEntity;
import com.engine.taskmanagement.task.activity.enums.TaskActivityType;
import com.engine.taskmanagement.task.entity.Task;
import com.engine.taskmanagement.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "task_activities")
@Getter
@Setter
@NoArgsConstructor
public class TaskActivity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id")
    private User actor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private TaskActivityType type;

    private String oldValue;

    private String newValue;

    @Column(length = 500)
    private String message;

    @Column(length = 1000)
    private String metadata;
}
