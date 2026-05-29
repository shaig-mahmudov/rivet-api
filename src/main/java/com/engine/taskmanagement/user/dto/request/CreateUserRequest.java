package com.engine.taskmanagement.user.dto.request;

import com.engine.taskmanagement.auth.enums.Role;
import com.engine.taskmanagement.project.entity.Project;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;

import java.util.ArrayList;
import java.util.List;

public class CreateUserRequest {
    private String username;

    @NotBlank(message = "Email is required" )
    private String email;

    @NotBlank(message = "Password is required")
    private String password;

    @NotBlank(message = "Confirm Password is required")
    private String confirmPassword;

    @Enumerated(EnumType.STRING)
    private Role role;
}
