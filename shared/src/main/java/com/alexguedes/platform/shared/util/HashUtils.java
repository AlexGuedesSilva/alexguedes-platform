package com.alexguedes.platform.shared.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.HexFormat;

public final class HashUtils {
    private HashUtils() {
    }

    public static String hmacSha256Base64(String secret, String payload) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return Base64.getEncoder().encodeToString(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("Could not calculate HMAC SHA-256", ex);
        }
    }

    public static String sha256Hex(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(orEmpty(value).getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("Could not calculate SHA-256", ex);
        }
    }

    public static boolean constantTimeEquals(String expected, String actual) {
        byte[] left = orEmpty(expected).getBytes(StandardCharsets.UTF_8);
        byte[] right = orEmpty(actual).getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(left, right);
    }

    public static String canonicalSignaturePayload(String method, String path, String body, long timestamp, String nonce) {
        return String.join("\n",
                orEmpty(method).toUpperCase(),
                orEmpty(path),
                sha256Hex(body),
                String.valueOf(timestamp),
                orEmpty(nonce)
        );
    }

    public static String orEmpty(String value) {
        return value == null ? "" : value;
    }
}
