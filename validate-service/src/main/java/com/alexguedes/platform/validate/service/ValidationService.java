package com.alexguedes.platform.validate.service;

import com.alexguedes.platform.shared.security.SignatureRequest;
import com.alexguedes.platform.validate.security.HmacValidator;
import com.alexguedes.platform.validate.security.ReplayAttackGuard;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
public class ValidationService {
    private final HmacValidator hmacValidator;
    private final ReplayAttackGuard replayAttackGuard;
    private final String clientKey;
    private final String clientSecret;
    private final Duration allowedSkew;
    private final Duration nonceTtl;

    public ValidationService(
            HmacValidator hmacValidator,
            ReplayAttackGuard replayAttackGuard,
            @Value("${app.hmac.client-key:demo-key}") String clientKey,
            @Value("${app.hmac.client-secret:demo-secret}") String clientSecret,
            @Value("${app.hmac.allowed-skew:PT5M}") Duration allowedSkew,
            @Value("${app.hmac.nonce-ttl:PT10M}") Duration nonceTtl
    ) {
        this.hmacValidator = hmacValidator;
        this.replayAttackGuard = replayAttackGuard;
        this.clientKey = clientKey;
        this.clientSecret = clientSecret;
        this.allowedSkew = allowedSkew;
        this.nonceTtl = nonceTtl;
    }

    public ValidationResult validate(SignatureRequest request) {
        if (!clientKey.equals(request.key())) {
            return new ValidationResult(false, "unknown client key");
        }
        if (request.timestamp() == null || isExpired(request.timestamp())) {
            return new ValidationResult(false, "timestamp outside allowed window");
        }
        if (request.nonce() == null || request.nonce().isBlank()) {
            return new ValidationResult(false, "nonce is required");
        }
        if (request.signature() == null || request.signature().isBlank()) {
            return new ValidationResult(false, "signature is required");
        }
        if (!hmacValidator.isValid(request, clientSecret)) {
            return new ValidationResult(false, "invalid signature");
        }
        if (!replayAttackGuard.reserve(request.key(), request.nonce(), nonceTtl)) {
            return new ValidationResult(false, "replay attack detected");
        }
        return new ValidationResult(true, "signature accepted");
    }

    private boolean isExpired(long epochSeconds) {
        Instant timestamp = Instant.ofEpochSecond(epochSeconds);
        Instant now = Instant.now();
        return timestamp.isBefore(now.minus(allowedSkew)) || timestamp.isAfter(now.plus(allowedSkew));
    }
}
