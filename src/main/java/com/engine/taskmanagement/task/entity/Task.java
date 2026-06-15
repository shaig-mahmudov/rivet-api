package com.engine.taskmanagement.task.entity;

import com.engine.taskmanagement.common.entity.BaseEntity;
import com.engine.taskmanagement.project.entity.Project;
import com.engine.taskmanagement.task.activity.entity.TaskActivity;
import com.engine.taskmanagement.task.comment.entity.TaskComment;
import com.engine.taskmanagement.task.criteria.entity.AcceptanceCriteria;
import com.engine.taskmanagement.task.dependency.entity.TaskDependency;
import com.engine.taskmanagement.task.enums.Severity;
import com.engine.taskmanagement.task.enums.TaskPriority;
import com.engine.taskmanagement.task.enums.TaskStatus;
import com.engine.taskmanagement.task.enums.TaskType;
import com.engine.taskmanagement.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tasks")
@Getter
@Setter
@NoArgsConstructor
public class Task extends BaseEntity {

    @Column(nullable = false)
    private String title;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskPriority priority = TaskPriority.MEDIUM;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status = TaskStatus.TODO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskType type;

    @Enumerated(EnumType.STRING)
    private Severity severity;

    @Column(length = 1000)
    private String technicalContext;

    @Column(length = 1000)
    private String expectedOutcome;

    private LocalDate dueDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_id")
    private User assignee;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TaskActivity> activities = new ArrayList<>();

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AcceptanceCriteria> acceptanceCriteria = new ArrayList<>();

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TaskComment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TaskDependency> dependencies = new ArrayList<>();

    @OneToMany(mappedBy = "dependsOnTask", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TaskDependency> blockedTasks = new ArrayList<>();

}
