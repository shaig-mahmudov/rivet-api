package com.engine.taskmanagement.auth.service;

import com.engine.taskmanagement.auth.dto.request.AdminBootstrapRequest;
import com.engine.taskmanagement.auth.dto.request.LoginRequest;
import com.engine.taskmanagement.auth.dto.request.RefreshTokenRequest;
import com.engine.taskmanagement.auth.dto.request.RegisterRequest;
import com.engine.taskmanagement.auth.dto.response.AuthResponse;
import com.engine.taskmanagement.auth.enums.Role;
import com.engine.taskmanagement.auth.token.dto.RefreshTokenIssue;
import com.engine.taskmanagement.auth.token.dto.RefreshTokenRotation;
import com.engine.taskmanagement.auth.token.service.RefreshTokenService;
import com.engine.taskmanagement.common.exception.BadRequestException;
import com.engine.taskmanagement.common.exception.DuplicateResourceException;
import com.engine.taskmanagement.common.exception.ForbiddenException;
import com.engine.taskmanagement.user.dto.response.UserResponse;
import com.engine.taskmanagement.user.entity.User;
import com.engine.taskmanagement.user.mapper.UserMapper;
import com.engine.taskmanagement.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final String adminBootstrapToken;

    public AuthServiceImpl(
            UserRepository userRepository,
            UserMapper userMapper,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            RefreshTokenService refreshTokenService,
            @Value("${app.security.admin-bootstrap.token:}") String adminBootstrapToken
    ) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.adminBootstrapToken = adminBootstrapToken;
    }

    @Override
    public AuthResponse bootstrapAdmin(AdminBootstrapRequest request) {
        validateAdminBootstrapToken(request.getBootstrapToken());

        if (userRepository.existsByRoleAndDeletedAtIsNull(Role.ADMIN)) {
            throw new BadRequestException("Admin bootstrap has already been completed");
        }

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Password and confirm password must match");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("User already exists with email: " + request.getEmail());
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.ADMIN);
        User savedUser = userRepository.save(user);

        return toAuthResponse("Admin bootstrap successful", savedUser, refreshTokenService.issueFor(savedUser));
    }

    @Override
    public AuthResponse register(RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Password and confirm password must match");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("User already exists with email: " + request.getEmail());
        }

        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.USER);
        User savedUser = userRepository.save(user);

        return toAuthResponse("Registration successful", savedUser, refreshTokenService.issueFor(savedUser));
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("Invalid email or password"));

        return toAuthResponse("Login successful", user, refreshTokenService.issueFor(user));
    }

    @Override
    public AuthResponse refresh(RefreshTokenRequest request) {
        RefreshTokenRotation rotation = refreshTokenService.rotate(request.getRefreshToken());
        return toAuthResponse("Token refreshed successfully", rotation.user(), rotation.refreshToken());
    }

    @Override
    public void logout(RefreshTokenRequest request) {
        refreshTokenService.revoke(request.getRefreshToken());
    }

    private void validateAdminBootstrapToken(String submittedToken) {
        if (adminBootstrapToken == null || adminBootstrapToken.isBlank()) {
            throw new BadRequestException("Admin bootstrap is not configured");
        }

        if (!constantTimeEquals(adminBootstrapToken, submittedToken)) {
            throw new ForbiddenException("Invalid admin bootstrap token");
        }
    }

    private AuthResponse toAuthResponse(String message, User user, RefreshTokenIssue refreshToken) {
        UserResponse response = userMapper.toResponse(user);
        return new AuthResponse(
                message,
                jwtService.generateToken(user),
                refreshToken.token(),
                "Bearer",
                jwtService.getExpirationSeconds(),
                refreshToken.expiresIn(),
                response
        );
    }

    private boolean constantTimeEquals(String expected, String actual) {
        if (actual == null) {
            return false;
        }

        return MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                actual.getBytes(StandardCharsets.UTF_8)
        );
    }
}
