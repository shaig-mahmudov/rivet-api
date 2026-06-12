package com.engine.taskmanagement.auth.service;

import com.engine.taskmanagement.auth.dto.request.LoginRequest;
import com.engine.taskmanagement.auth.dto.response.AuthResponse;
import com.engine.taskmanagement.user.dto.request.CreateUserRequest;

public interface AuthService {
    AuthResponse register(CreateUserRequest request);
    AuthResponse login(LoginRequest request);
}
