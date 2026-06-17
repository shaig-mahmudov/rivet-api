package com.engine.taskmanagement.auth.token.service;

import java.time.Duration;

public interface TokenBlacklistService {
    void blacklist(String token, Duration ttl);

    boolean isBlacklisted(String token);
}
