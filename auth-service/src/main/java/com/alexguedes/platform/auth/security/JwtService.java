package com.alexguedes.platform.auth.security;

import com.alexguedes.platform.auth.domain.model.UserEntity;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {
    private final SecretKey secretKey;
    private final long expiresInSeconds;

    public JwtService(
            @Value("${app.jwt.secret:change-me-to-a-very-long-secret-key-32-bytes}") String secret,
            @Value("${app.jwt.expires-in-seconds:3600}") long expiresInSeconds
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiresInSeconds = expiresInSeconds;
    }

    public String generateToken(String subject) {
        return generateToken(subject, null, null);
    }

    public String generateToken(UserEntity user) {
        return generateToken(user.getUsername(), user.getId().toString(), user.getRole());
    }

    public String extractUsername(String token) {
        return claims(token).getSubject();
    }

    private String generateToken(String subject, String userId, String role) {
        Instant now = Instant.now();
        var builder = Jwts.builder()
                .subject(subject)
                .issuer("auth-service")
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(expiresInSeconds)))
                .signWith(secretKey);

        if (userId != null) {
            builder.claim("userId", userId);
        }
        if (role != null) {
            builder.claim("role", role);
        }

        return builder.compact();
    }

    private Claims claims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public long expiresInSeconds() {
        return expiresInSeconds;
    }
}
