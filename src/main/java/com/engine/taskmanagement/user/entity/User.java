package com.engine.taskmanagement.user.entity;

import com.engine.taskmanagement.auth.enums.Role;
import com.engine.taskmanagement.common.entity.BaseEntity;
import com.engine.taskmanagement.project.entity.Project;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Table(name = "users")
@Entity
@Getter
@Setter
@NoArgsConstructor
public class User extends BaseEntity {

    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    @OneToMany(mappedBy = "owner")
    private List<Project> projects = new ArrayList<>();
}
