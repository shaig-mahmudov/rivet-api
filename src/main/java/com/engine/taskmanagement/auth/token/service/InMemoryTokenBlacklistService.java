package com.engine.taskmanagement.auth.token.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@ConditionalOnProperty(
        name = "app.security.token-blacklist.store",
        havingValue = "memory",
        matchIfMissing = true
)
public class InMemoryTokenBlacklistService implements TokenBlacklistService {

    private final TokenHashService tokenHashService;
    private final Map<String, Instant> blacklistedTokens = new ConcurrentHashMap<>();

    public InMemoryTokenBlacklistService(TokenHashService tokenHashService) {
        this.tokenHashService = tokenHashService;
    }

    @Override
    public void blacklist(String token, Duration ttl) {
        if (ttl.isZero() || ttl.isNegative()) {
            return;
        }
        blacklistedTokens.put(tokenHashService.hash(token), Instant.now().plus(ttl));
    }

    @Override
    public boolean isBlacklisted(String token) {
        String tokenHash = tokenHashService.hash(token);
        Instant expiresAt = blacklistedTokens.get(tokenHash);
        if (expiresAt == null) {
            return false;
        }
        if (Instant.now().isAfter(expiresAt)) {
            blacklistedTokens.remove(tokenHash);
            return false;
        }
        return true;
    }
}
