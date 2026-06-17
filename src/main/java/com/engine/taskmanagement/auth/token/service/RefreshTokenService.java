package com.engine.taskmanagement.auth.token.service;

import com.engine.taskmanagement.auth.token.dto.RefreshTokenIssue;
import com.engine.taskmanagement.auth.token.dto.RefreshTokenRotation;
import com.engine.taskmanagement.auth.token.entity.RefreshToken;
import com.engine.taskmanagement.auth.token.repository.RefreshTokenRepository;
import com.engine.taskmanagement.common.exception.UnauthorizedException;
import com.engine.taskmanagement.user.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
public class RefreshTokenService {

    private static final int TOKEN_BYTE_LENGTH = 32;

    private final RefreshTokenRepository refreshTokenRepository;
    private final SecureRandom secureRandom = new SecureRandom();
    private final long expirationSeconds;
    private final long revokedTokenRetentionSeconds;

    public RefreshTokenService(
            RefreshTokenRepository refreshTokenRepository,
            @Value("${app.security.refresh-token.expiration-seconds:2592000}") long expirationSeconds,
            @Value("${app.security.refresh-token.revoked-retention-seconds:86400}") long revokedTokenRetentionSeconds
    ) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.expirationSeconds = expirationSeconds;
        this.revokedTokenRetentionSeconds = revokedTokenRetentionSeconds;
    }

    @Transactional
    public RefreshTokenIssue issueFor(User user) {
        String rawToken = generateToken();
        String tokenHash = hash(rawToken);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setTokenHash(tokenHash);
        refreshToken.setExpiresAt(LocalDateTime.now().plusSeconds(expirationSeconds));
        refreshTokenRepository.save(refreshToken);

        return new RefreshTokenIssue(rawToken, tokenHash, expirationSeconds);
    }

    @Transactional
    public RefreshTokenRotation rotate(String rawToken) {
        RefreshToken existingToken = findValidToken(rawToken);
        RefreshTokenIssue newRefreshToken = issueFor(existingToken.getUser());
        existingToken.revoke(newRefreshToken.tokenHash());

        return new RefreshTokenRotation(existingToken.getUser(), newRefreshToken);
    }

    @Transactional
    public void revoke(String rawToken) {
        RefreshToken refreshToken = findValidToken(rawToken);
        refreshToken.revoke(null);
    }

    @Scheduled(fixedDelayString = "${app.security.refresh-token.cleanup-interval-ms:3600000}")
    @Transactional
    public void scheduledCleanup() {
        cleanupExpiredAndRevokedTokens();
    }

    @Transactional
    public long cleanupExpiredAndRevokedTokens() {
        LocalDateTime now = LocalDateTime.now();
        long expiredCount = refreshTokenRepository.deleteByExpiresAtBefore(now);
        long revokedCount = refreshTokenRepository.deleteByRevokedAtIsNotNullAndRevokedAtBefore(
                now.minusSeconds(revokedTokenRetentionSeconds)
        );
        return expiredCount + revokedCount;
    }

    private RefreshToken findValidToken(String rawToken) {
        LocalDateTime now = LocalDateTime.now();
        RefreshToken refreshToken = refreshTokenRepository.findByTokenHashForUpdate(hash(rawToken))
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        if (refreshToken.isRevoked() || refreshToken.isExpired(now) || refreshToken.getUser().isDeleted()) {
            throw new UnauthorizedException("Invalid refresh token");
        }

        return refreshToken;
    }

    private String generateToken() {
        byte[] tokenBytes = new byte[TOKEN_BYTE_LENGTH];
        secureRandom.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }

    private String hash(String rawToken) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (Exception ex) {
            throw new IllegalStateException("Could not hash refresh token", ex);
        }
    }
}
