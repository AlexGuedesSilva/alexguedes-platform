package com.alexguedes.platform.validate.security;

import com.alexguedes.platform.shared.security.SignatureRequest;
import com.alexguedes.platform.shared.util.HashUtils;
import org.springframework.stereotype.Component;

@Component
public class HmacValidator {
    public boolean isValid(SignatureRequest request, String secret) {
        String payload = HashUtils.canonicalSignaturePayload(
                request.method(),
                request.path(),
                request.body(),
                request.timestamp(),
                request.nonce()
        );
        String expected = HashUtils.hmacSha256Base64(secret, payload);
        return HashUtils.constantTimeEquals(expected, request.signature());
    }
}
