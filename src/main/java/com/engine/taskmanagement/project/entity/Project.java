package com.engine.taskmanagement.project.entity;

import com.engine.taskmanagement.common.entity.BaseEntity;
import com.engine.taskmanagement.task.entity.Task;
import com.engine.taskmanagement.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Table(name = "projects")
@Entity
@Getter
@Setter
@NoArgsConstructor
public class Project extends BaseEntity {

    private String name;

    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private User owner;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Task> tasks = new ArrayList<>();

    public void markAsDeletedWithTasks() {
        markAsDeleted();
        tasks.stream()
                .filter(task -> !task.isDeleted())
                .forEach(Task::markAsDeleted);
    }
}
