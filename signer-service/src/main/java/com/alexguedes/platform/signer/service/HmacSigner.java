package com.alexguedes.platform.signer.service;

import com.alexguedes.platform.shared.security.SignatureRequest;
import com.alexguedes.platform.shared.security.SignatureResponse;
import com.alexguedes.platform.shared.util.HashUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class HmacSigner {
    private final String fallbackSecret;

    public HmacSigner(@Value("${app.hmac.client-secret:demo-secret}") String fallbackSecret) {
        this.fallbackSecret = fallbackSecret;
    }

    public SignatureResponse sign(SignatureRequest request) {
        long timestamp = request.timestamp() == null ? Instant.now().getEpochSecond() : request.timestamp();
        String nonce = request.nonce() == null || request.nonce().isBlank() ? UUID.randomUUID().toString() : request.nonce();
        String secret = request.secret() == null || request.secret().isBlank() ? fallbackSecret : request.secret();
        String payload = HashUtils.canonicalSignaturePayload(
                request.method(),
                request.path(),
                request.body(),
                timestamp,
                nonce
        );

        return new SignatureResponse(
                request.key(),
                request.method().toUpperCase(),
                request.path(),
                HashUtils.sha256Hex(request.body()),
                timestamp,
                nonce,
                HashUtils.hmacSha256Base64(secret, payload)
        );
    }
}
