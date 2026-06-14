package com.engine.taskmanagement.task.comment.entity;

import com.engine.taskmanagement.common.entity.BaseEntity;
import com.engine.taskmanagement.task.comment.enums.TaskCommentType;
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
@Table(name = "task_comments")
@Getter
@Setter
@NoArgsConstructor
public class TaskComment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private TaskCommentType type = TaskCommentType.GENERAL;

    @Column(nullable = false, length = 2000)
    private String body;
}
