package com.engine.taskmanagement.user.dto.request;

import com.engine.taskmanagement.auth.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserRequest {
    @Size(max = 255, message = "Username cannot exceed 255 characters")
    private String username;

    @Email(message = "Email must be valid")
    private String email;

    private Role role;
}
