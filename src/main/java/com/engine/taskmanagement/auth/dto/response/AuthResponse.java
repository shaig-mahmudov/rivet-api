package com.engine.taskmanagement.auth.dto.response;

import com.engine.taskmanagement.user.dto.response.UserResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthResponse {
    private String message;
    private UserResponse user;
}
