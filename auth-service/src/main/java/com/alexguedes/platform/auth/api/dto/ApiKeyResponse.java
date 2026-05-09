package com.alexguedes.platform.auth.api.dto;

import java.time.Instant;
import java.util.UUID;

public record ApiKeyResponse(
        UUID id,
        String key,
        String prefix,
        String name,
        Instant createdAt
) {
}
