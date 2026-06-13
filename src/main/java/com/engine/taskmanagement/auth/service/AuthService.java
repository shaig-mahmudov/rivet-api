package com.engine.taskmanagement.auth.service;

import com.engine.taskmanagement.auth.dto.request.LoginRequest;
import com.engine.taskmanagement.auth.dto.request.RegisterRequest;
import com.engine.taskmanagement.auth.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
}
