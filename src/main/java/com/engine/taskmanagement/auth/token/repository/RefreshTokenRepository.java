package com.engine.taskmanagement.auth.token.repository;

import com.engine.taskmanagement.auth.token.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByTokenHash(String tokenHash);

    List<RefreshToken> findAllByUser_IdAndRevokedAtIsNull(Long userId);
}
