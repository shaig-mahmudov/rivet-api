package com.engine.taskmanagement.user.dto.response;

import com.engine.taskmanagement.auth.enums.Role;
import com.engine.taskmanagement.project.entity.Project;
import java.util.ArrayList;
import java.util.List;

public class UserResponse {

    private String username;
    private String email;
    private String password;
    private Role role;
    private List<Project> projects = new ArrayList<>();
}
