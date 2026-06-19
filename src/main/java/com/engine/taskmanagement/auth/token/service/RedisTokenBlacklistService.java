package com.engine.taskmanagement.auth.token.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@ConditionalOnProperty(name = "app.security.token-blacklist.store", havingValue = "redis")
public class RedisTokenBlacklistService implements TokenBlacklistService {

    private static final String KEY_PREFIX = "auth:blacklist:";

    private final StringRedisTemplate redisTemplate;
    private final TokenHashService tokenHashService;

    public RedisTokenBlacklistService(StringRedisTemplate redisTemplate, TokenHashService tokenHashService) {
        this.redisTemplate = redisTemplate;
        this.tokenHashService = tokenHashService;
    }

    @Override
    public void blacklist(String token, Duration ttl) {
        if (ttl.isZero() || ttl.isNegative()) {
            return;
        }
        redisTemplate.opsForValue().set(key(token), "1", ttl);
    }

    @Override
    public boolean isBlacklisted(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key(token)));
    }

    private String key(String token) {
        return KEY_PREFIX + tokenHashService.hash(token);
    }
}
