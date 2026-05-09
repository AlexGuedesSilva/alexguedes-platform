package com.alexguedes.platform.auth.api.dto;

import jakarta.validation.constraints.Size;

public record ApiKeyRequest(
        @Size(max = 100) String name
) {
}
