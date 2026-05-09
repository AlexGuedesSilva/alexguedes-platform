package com.alexguedes.platform.auth.api.dto;

import java.time.Instant;
import java.util.UUID;

public record RegisterResponse(
        UUID id,
        String username,
        String email,
        String role,
        Instant createdAt
) {
}
