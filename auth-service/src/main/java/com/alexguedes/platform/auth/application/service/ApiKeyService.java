package com.alexguedes.platform.auth.application.service;

import com.alexguedes.platform.auth.api.dto.ApiKeyRequest;
import com.alexguedes.platform.auth.api.dto.ApiKeyResponse;
import com.alexguedes.platform.auth.domain.model.ApiKeyEntity;
import com.alexguedes.platform.auth.domain.model.UserEntity;
import com.alexguedes.platform.auth.infrastructure.repository.ApiKeyRepository;
import com.alexguedes.platform.auth.infrastructure.repository.UserRepository;
import com.alexguedes.platform.auth.security.JwtService;
import com.alexguedes.platform.shared.util.HashUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

@Service
public class ApiKeyService {

    private final ApiKeyRepository apiKeyRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final SecureRandom secureRandom = new SecureRandom();

    public ApiKeyService(
            ApiKeyRepository apiKeyRepository,
            UserRepository userRepository,
            JwtService jwtService
    ) {
        this.apiKeyRepository = apiKeyRepository;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    @Transactional
    public ApiKeyResponse create(String authorizationHeader, ApiKeyRequest request) {
        UserEntity user = authenticatedUser(authorizationHeader);
        String rawKey = generateRawApiKey();
        Instant now = Instant.now();

        ApiKeyEntity apiKey = new ApiKeyEntity();
        apiKey.setId(UUID.randomUUID());
        apiKey.setUser(user);
        apiKey.setName(normalizeName(request));
        apiKey.setKeyHash(HashUtils.sha256Hex(rawKey));
        apiKey.setKeyPrefix(rawKey.substring(0, Math.min(rawKey.length(), 12)));
        apiKey.setEnabled(true);
        apiKey.setCreatedAt(now);

        apiKeyRepository.save(apiKey);

        return new ApiKeyResponse(
                apiKey.getId(),
                rawKey,
                apiKey.getKeyPrefix(),
                apiKey.getName(),
                apiKey.getCreatedAt()
        );
    }

    private UserEntity authenticatedUser(String authorizationHeader) {
        String token = extractBearerToken(authorizationHeader);
        String username = jwtService.extractUsername(token);
        return userRepository.findByUsername(username)
                .filter(user -> Boolean.TRUE.equals(user.getEnabled()))
                .orElseThrow(() -> new AccessDeniedException("invalid token subject"));
    }

    private String extractBearerToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new AccessDeniedException("missing bearer token");
        }
        return authorizationHeader.substring("Bearer ".length()).trim();
    }

    private String generateRawApiKey() {
        byte[] secret = new byte[32];
        secureRandom.nextBytes(secret);
        return "ak_" + Base64.getUrlEncoder().withoutPadding().encodeToString(secret);
    }

    private String normalizeName(ApiKeyRequest request) {
        if (request == null || request.name() == null || request.name().isBlank()) {
            return "default";
        }
        return request.name().trim();
    }
}
