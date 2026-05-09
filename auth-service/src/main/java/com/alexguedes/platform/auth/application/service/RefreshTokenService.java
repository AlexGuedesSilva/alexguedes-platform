package com.alexguedes.platform.auth.application.service;

import com.alexguedes.platform.auth.domain.model.RefreshTokenEntity;
import com.alexguedes.platform.auth.domain.model.UserEntity;
import com.alexguedes.platform.auth.infrastructure.repository.RefreshTokenRepository;
import com.alexguedes.platform.shared.util.HashUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final long refreshTokenExpiresInSeconds;
    private final SecureRandom secureRandom = new SecureRandom();

    public RefreshTokenService(
            RefreshTokenRepository refreshTokenRepository,
            @Value("${app.refresh-token.expires-in-seconds:604800}") long refreshTokenExpiresInSeconds
    ) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.refreshTokenExpiresInSeconds = refreshTokenExpiresInSeconds;
    }

    @Transactional
    public String issue(UserEntity user) {
        String rawToken = generateRawRefreshToken();
        Instant now = Instant.now();

        RefreshTokenEntity refreshToken = new RefreshTokenEntity();
        refreshToken.setId(UUID.randomUUID());
        refreshToken.setUser(user);
        refreshToken.setTokenHash(HashUtils.sha256Hex(rawToken));
        refreshToken.setCreatedAt(now);
        refreshToken.setExpiresAt(now.plusSeconds(refreshTokenExpiresInSeconds));

        refreshTokenRepository.save(refreshToken);
        return rawToken;
    }

    @Transactional
    public RefreshTokenRotation rotate(String rawRefreshToken) {
        Instant now = Instant.now();
        RefreshTokenEntity currentToken = refreshTokenRepository.findByTokenHash(HashUtils.sha256Hex(rawRefreshToken))
                .filter(token -> token.isActive(now))
                .orElseThrow(() -> new AccessDeniedException("invalid refresh token"));

        currentToken.setRevokedAt(now);
        refreshTokenRepository.save(currentToken);

        String newRefreshToken = issue(currentToken.getUser());
        return new RefreshTokenRotation(currentToken.getUser(), newRefreshToken);
    }

    @Transactional
    public void revoke(String rawRefreshToken) {
        Instant now = Instant.now();
        refreshTokenRepository.findByTokenHash(HashUtils.sha256Hex(rawRefreshToken))
                .filter(token -> token.getRevokedAt() == null)
                .ifPresent(token -> {
                    token.setRevokedAt(now);
                    refreshTokenRepository.save(token);
                });
    }

    private String generateRawRefreshToken() {
        byte[] secret = new byte[48];
        secureRandom.nextBytes(secret);
        return "rt_" + Base64.getUrlEncoder().withoutPadding().encodeToString(secret);
    }

    public record RefreshTokenRotation(UserEntity user, String refreshToken) {
    }
}
