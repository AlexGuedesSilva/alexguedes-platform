package com.alexguedes.platform.shared.security;

public record SignatureResponse(
        String key,
        String method,
        String path,
        String bodyHash,
        long timestamp,
        String nonce,
        String signature
) {
}
