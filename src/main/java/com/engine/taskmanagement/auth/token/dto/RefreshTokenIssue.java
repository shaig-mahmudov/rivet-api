package com.engine.taskmanagement.auth.token.dto;

public record RefreshTokenIssue(String token, String tokenHash, long expiresIn) {
}
