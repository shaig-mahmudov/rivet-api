package com.engine.taskmanagement.auth.service;

import com.engine.taskmanagement.auth.dto.request.AdminBootstrapRequest;
import com.engine.taskmanagement.auth.dto.request.LoginRequest;
import com.engine.taskmanagement.auth.dto.request.RefreshTokenRequest;
import com.engine.taskmanagement.auth.dto.request.RegisterRequest;
import com.engine.taskmanagement.auth.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse bootstrapAdmin(AdminBootstrapRequest request);
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    AuthResponse refresh(RefreshTokenRequest request);
    void logout(RefreshTokenRequest request, String authorizationHeader);
}
