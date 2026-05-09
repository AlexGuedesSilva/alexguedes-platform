package com.alexguedes.platform.signer.service;

import com.alexguedes.platform.shared.security.SignatureRequest;
import com.alexguedes.platform.shared.security.SignatureResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HmacSignerTest {

    private final HmacSigner signer = new HmacSigner("test-secret");

    @Test
    void shouldGenerateSignatureWithDefaults() {
        SignatureRequest request = new SignatureRequest(
                "test-key",
                null,
                "POST",
                "/sign",
                "{\"data\":\"123\"}",
                null,
                null,
                null
        );

        SignatureResponse response = signer.sign(request);

        assertNotNull(response);
        assertNotNull(response.signature());
        assertNotNull(response.nonce());
        assertTrue(response.timestamp() > 0);
    }

    @Test
    void shouldUseProvidedTimestampAndNonce() {
        SignatureRequest request = new SignatureRequest(
                "test-key",
                "secret",
                "POST",
                "/sign",
                "{}",
                123456789L,
                "fixed-nonce",
                null
        );

        SignatureResponse response = signer.sign(request);

        assertEquals(123456789L, response.timestamp());
        assertEquals("fixed-nonce", response.nonce());
    }

    @Test //Teste de fallback do secret
    void shouldUseFallbackSecretWhenSecretIsNull() {
        HmacSigner signer = new HmacSigner("fallback-secret");

        SignatureRequest requestWithNullSecret = new SignatureRequest(
                "key",
                null,
                "POST",
                "/test",
                "{}",
                123L,
                "nonce",
                null
        );

        SignatureRequest requestWithFallbackExplicit = new SignatureRequest(
                "key",
                "fallback-secret",
                "POST",
                "/test",
                "{}",
                123L,
                "nonce",
                null
        );

        String sig1 = signer.sign(requestWithNullSecret).signature();
        String sig2 = signer.sign(requestWithFallbackExplicit).signature();

        assertEquals(sig1, sig2);
    }

    @Test //Teste de geração automática de nonce
    void shouldGenerateNonceWhenNull() {
        HmacSigner signer = new HmacSigner("secret");

        SignatureRequest request = new SignatureRequest(
                "key",
                "secret",
                "POST",
                "/test",
                "{}",
                123L,
                null, // 🔥 importante
                null
        );

        var response = signer.sign(request);

        assertNotNull(response.nonce());
    }

    @Test //Teste de timestamp automático
    void shouldGenerateTimestampWhenNull() {
        HmacSigner signer = new HmacSigner("secret");

        SignatureRequest request = new SignatureRequest(
                "key",
                "secret",
                "POST",
                "/test",
                "{}",
                null, // 🔥 aqui
                "nonce",
                null
        );

        var response = signer.sign(request);

        assertTrue(response.timestamp() > 0);
    }

    @Test // Teste: Assinatura deve ser determinístico
    void shouldGenerateSameSignatureForSameInput() {
        HmacSigner signer = new HmacSigner("secret");

        SignatureRequest request = new SignatureRequest(
                "key",
                "secret",
                "POST",
                "/test",
                "{\"a\":1}",
                123L,
                "nonce-fixed",
                null
        );

        SignatureResponse r1 = signer.sign(request);
        SignatureResponse r2 = signer.sign(request);

        assertEquals(r1.signature(), r2.signature());
    }

    @Test // Teste: payload alterado deve mudar assinatura
    void shouldGenerateDifferentSignatureWhenBodyChanges() {
        HmacSigner signer = new HmacSigner("secret");

        SignatureRequest req1 = new SignatureRequest(
                "key",
                "secret",
                "POST",
                "/test",
                "{\"a\":1}",
                123L,
                "nonce",
                null
        );

        SignatureRequest req2 = new SignatureRequest(
                "key",
                "secret",
                "POST",
                "/test",
                "{\"a\":2}",
                123L,
                "nonce",
                null
        );

        String sig1 = signer.sign(req1).signature();
        String sig2 = signer.sign(req2).signature();

        assertNotEquals(sig1, sig2);
    }

    @Test //Teste: método HTTP influencia assinatura
    void shouldChangeSignatureWhenMethodChanges() {
        HmacSigner signer = new HmacSigner("secret");

        SignatureRequest post = new SignatureRequest(
                "key",
                "secret",
                "POST",
                "/test",
                "{}",
                123L,
                "nonce",
                null
        );

        SignatureRequest get = new SignatureRequest(
                "key",
                "secret",
                "GET",
                "/test",
                "{}",
                123L,
                "nonce",
                null
        );

        assertNotEquals(
                signer.sign(post).signature(),
                signer.sign(get).signature()
        );
    }

    @Test // Teste: nonce diferente -> assinatura diferente
    void shouldChangeSignatureWhenNonceChanges() {
        HmacSigner signer = new HmacSigner("secret");

        SignatureRequest r1 = new SignatureRequest(
                "key",
                "secret",
                "POST",
                "/test",
                "{}",
                123L,
                "nonce1",
                null
        );

        SignatureRequest r2 = new SignatureRequest(
                "key",
                "secret",
                "POST",
                "/test",
                "{}",
                123L,
                "nonce2",
                null
        );

        assertNotEquals(
                signer.sign(r1).signature(),
                signer.sign(r2).signature()
        );
    }

    @Test // Teste: timestamp influencia assinatura
    void shouldChangeSignatureWhenTimestampChanges() {
        HmacSigner signer = new HmacSigner("secret");

        SignatureRequest r1 = new SignatureRequest(
                "key",
                "secret",
                "POST",
                "/test",
                "{}",
                100L,
                "nonce",
                null
        );

        SignatureRequest r2 = new SignatureRequest(
                "key",
                "secret",
                "POST",
                "/test",
                "{}",
                200L,
                "nonce",
                null
        );

        assertNotEquals(
                signer.sign(r1).signature(),
                signer.sign(r2).signature()
        );
    }

    @Test
    void shouldNormalizeHttpMethodToUppercase() {
        HmacSigner signer = new HmacSigner("secret");

        SignatureRequest lower = new SignatureRequest(
                "key",
                "secret",
                "post",
                "/test",
                "{}",
                123L,
                "nonce",
                null
        );

        SignatureRequest upper = new SignatureRequest(
                "key",
                "secret",
                "POST",
                "/test",
                "{}",
                123L,
                "nonce",
                null
        );

        assertEquals(
                signer.sign(lower).signature(),
                signer.sign(upper).signature()
        );
    }
}
