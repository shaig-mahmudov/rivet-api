package com.engine.taskmanagement.auth.token.dto;

import com.engine.taskmanagement.user.entity.User;

public record RefreshTokenRotation(User user, RefreshTokenIssue refreshToken) {
}
