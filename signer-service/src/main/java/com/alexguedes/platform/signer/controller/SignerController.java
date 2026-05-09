package com.alexguedes.platform.signer.controller;

import com.alexguedes.platform.shared.dto.ApiResponse;
import com.alexguedes.platform.shared.security.SignatureRequest;
import com.alexguedes.platform.shared.security.SignatureResponse;
import com.alexguedes.platform.signer.service.HmacSigner;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sign")
public class SignerController {
    private final HmacSigner hmacSigner;

    public SignerController(HmacSigner hmacSigner) {
        this.hmacSigner = hmacSigner;
    }

    @PostMapping
    public ApiResponse<SignatureResponse> sign(@Valid @RequestBody SignatureRequest request) {
        return ApiResponse.ok("signature generated", hmacSigner.sign(request));
    }
}
