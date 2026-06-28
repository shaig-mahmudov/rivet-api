package com.engine.taskmanagement.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitFilter.class);

    private final StringRedisTemplate redisTemplate;
    
    // In-memory fallback in case Redis is not available (e.g. tests or local dev without Redis)
    private final ConcurrentHashMap<String, AtomicInteger> localCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> localExpiry = new ConcurrentHashMap<>();

    private static final int MAX_REQUESTS_PER_MINUTE = 10;
    private static final long WINDOW_MS = 60000;

    public RateLimitFilter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String path = request.getRequestURI();

        // Only rate limit auth endpoints
        if (path != null && path.startsWith("/api/auth/")) {
            String ip = getClientIp(request);
            String key = "rate:limit:" + ip + ":" + path;

            boolean allowed = isAllowed(key);
            if (!allowed) {
                log.warn("Rate limit exceeded for IP: {} on path: {}", ip, path);
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"Too many requests. Please try again later.\"}");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isAllowed(String key) {
        try {
            Long count = redisTemplate.opsForValue().increment(key, 1);
            if (count != null && count == 1) {
                redisTemplate.expire(key, Duration.ofMinutes(1));
            }
            return count != null && count <= MAX_REQUESTS_PER_MINUTE;
        } catch (Exception e) {
            log.debug("Redis rate limiter failed, falling back to in-memory: {}", e.getMessage());
            return isAllowedInMemory(key);
        }
    }

    private synchronized boolean isAllowedInMemory(String key) {
        long now = System.currentTimeMillis();
        Long expiry = localExpiry.get(key);

        if (expiry == null || now > expiry) {
            localCache.put(key, new AtomicInteger(1));
            localExpiry.put(key, now + WINDOW_MS);
            return true;
        }

        AtomicInteger count = localCache.get(key);
        if (count == null) {
            localCache.put(key, new AtomicInteger(1));
            return true;
        }

        int currentCount = count.incrementAndGet();
        return currentCount <= MAX_REQUESTS_PER_MINUTE;
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader != null && !xfHeader.isBlank()) {
            return xfHeader.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
