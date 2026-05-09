package com.alexguedes.platform.signer.controller;

import com.alexguedes.platform.signer.service.HmacSigner;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SignerController.class)
class SignerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HmacSigner hmacSigner;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldReturnSignature() throws Exception {
        String requestJson = """
        {
            "key": "test-key",
            "method": "POST",
            "path": "/sign",
            "body": "{}"
        }
        """;

        when(hmacSigner.sign(org.mockito.ArgumentMatchers.any()))
                .thenReturn(new com.alexguedes.platform.shared.security.SignatureResponse(
                        "test-key",
                        "POST",
                        "/sign",
                        "hash",
                        123L,
                        "nonce",
                        "signature"
                ));

        mockMvc.perform(post("/sign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.signature").value("signature"));
    }
}