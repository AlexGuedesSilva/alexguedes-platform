package com.alexguedes.platform.validate.service;

import com.alexguedes.platform.shared.security.SignatureRequest;
import com.alexguedes.platform.validate.security.HmacValidator;
import com.alexguedes.platform.validate.security.ReplayAttackGuard;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ValidationServiceTest {

    @Mock
    private HmacValidator hmacValidator;

    @Mock
    private ReplayAttackGuard replayAttackGuard;

    private ValidationService service;

    @BeforeEach
    void setUp() {
        service = new ValidationService(
                hmacValidator,
                replayAttackGuard,
                "client-key",
                "client-secret",
                Duration.ofMinutes(5),
                Duration.ofMinutes(10)
        );
    }

    @Test
    void shouldAcceptRequest_whenAllFieldsAreValid() {
        // given
        SignatureRequest request = new SignatureRequest(
                "client-key",
                null,
                "POST",
                "/test",
                "{}",
                Instant.now().getEpochSecond(),
                "nonce",
                "valid-signature"
        );

        when(hmacValidator.isValid(any(), eq("client-secret"))).thenReturn(true);
        when(replayAttackGuard.reserve(any(), any(), any())).thenReturn(true);

        // when
        ValidationResult result = service.validate(request);

        // then
        assertTrue(result.valid());
        assertEquals("signature accepted", result.message());
    }

    @Test
    void shouldRejectRequest_whenClientKeyIsInvalid() {
        SignatureRequest request = new SignatureRequest(
                "wrong-key",
                null,
                "POST",
                "/test",
                "{}",
                Instant.now().getEpochSecond(),
                "nonce",
                "sig"
        );

        ValidationResult result = service.validate(request);

        assertFalse(result.valid());
        assertEquals("unknown client key", result.message());
    }

    @Test
    void shouldRejectRequest_whenTimestampIsExpired() {
        long expiredTimestamp = Instant.now().minusSeconds(600).getEpochSecond();

        SignatureRequest request = new SignatureRequest(
                "client-key",
                null,
                "POST",
                "/test",
                "{}",
                expiredTimestamp,
                "nonce",
                "sig"
        );

        ValidationResult result = service.validate(request);

        assertFalse(result.valid());
        assertEquals("timestamp outside allowed window", result.message());
    }

    @Test
    void shouldRejectRequest_whenTimestampIsNull() {
        SignatureRequest request = new SignatureRequest(
                "client-key",
                null,
                "POST",
                "/test",
                "{}",
                null,
                "nonce",
                "sig"
        );

        ValidationResult result = service.validate(request);

        assertFalse(result.valid());
        assertEquals("timestamp outside allowed window", result.message());
    }

    @Test
    void shouldRejectRequest_whenNonceIsMissing() {
        SignatureRequest request = new SignatureRequest(
                "client-key",
                null,
                "POST",
                "/test",
                "{}",
                Instant.now().getEpochSecond(),
                null,
                "sig"
        );

        ValidationResult result = service.validate(request);

        assertFalse(result.valid());
        assertEquals("nonce is required", result.message());
    }

    @Test
    void shouldRejectRequest_whenNonceIsBlank() {
        SignatureRequest request = new SignatureRequest(
                "client-key",
                null,
                "POST",
                "/test",
                "{}",
                Instant.now().getEpochSecond(),
                "",
                "sig"
        );

        ValidationResult result = service.validate(request);

        assertFalse(result.valid());
        assertEquals("nonce is required", result.message());
    }

    @Test
    void shouldRejectRequest_whenSignatureIsMissing() {
        SignatureRequest request = new SignatureRequest(
                "client-key",
                null,
                "POST",
                "/test",
                "{}",
                Instant.now().getEpochSecond(),
                "nonce",
                null
        );

        ValidationResult result = service.validate(request);

        assertFalse(result.valid());
        assertEquals("signature is required", result.message());
    }

    @Test
    void shouldRejectRequest_whenSignatureIsBlank() {
        SignatureRequest request = new SignatureRequest(
                "client-key",
                null,
                "POST",
                "/test",
                "{}",
                Instant.now().getEpochSecond(),
                "nonce",
                ""
        );

        ValidationResult result = service.validate(request);

        assertFalse(result.valid());
        assertEquals("signature is required", result.message());
    }

    @Test
    void shouldRejectRequest_whenSignatureIsInvalid() {
        SignatureRequest request = new SignatureRequest(
                "client-key",
                null,
                "POST",
                "/test",
                "{}",
                Instant.now().getEpochSecond(),
                "nonce",
                "invalid-signature"
        );

        when(hmacValidator.isValid(any(), eq("client-secret"))).thenReturn(false);

        ValidationResult result = service.validate(request);

        assertFalse(result.valid());
        assertEquals("invalid signature", result.message());
    }

    @Test
    void shouldRejectRequest_whenReplayAttackDetected() {
        SignatureRequest request = new SignatureRequest(
                "client-key",
                null,
                "POST",
                "/test",
                "{}",
                Instant.now().getEpochSecond(),
                "same-nonce",
                "valid-signature"
        );

        when(hmacValidator.isValid(any(), any())).thenReturn(true);
        when(replayAttackGuard.reserve(any(), any(), any())).thenReturn(false);

        ValidationResult result = service.validate(request);

        assertFalse(result.valid());
        assertEquals("replay attack detected", result.message());
    }
}