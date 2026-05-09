package com.alexguedes.platform.shared.security;

import jakarta.validation.constraints.NotBlank;

public record SignatureRequest(
        @NotBlank String key,
        String secret,
        @NotBlank String method,
        @NotBlank String path,
        String body,
        Long timestamp,
        String nonce,
        String signature
) {
}
