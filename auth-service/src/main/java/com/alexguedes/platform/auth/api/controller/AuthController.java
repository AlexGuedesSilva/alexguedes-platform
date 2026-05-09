package com.alexguedes.platform.auth.api.controller;

import com.alexguedes.platform.auth.api.dto.ApiKeyRequest;
import com.alexguedes.platform.auth.api.dto.ApiKeyResponse;
import com.alexguedes.platform.auth.api.dto.LoginRequest;
import com.alexguedes.platform.auth.api.dto.LogoutRequest;
import com.alexguedes.platform.auth.api.dto.RefreshTokenRequest;
import com.alexguedes.platform.auth.api.dto.RegisterRequest;
import com.alexguedes.platform.auth.api.dto.RegisterResponse;
import com.alexguedes.platform.auth.api.dto.TokenResponse;
import com.alexguedes.platform.auth.application.service.ApiKeyService;
import com.alexguedes.platform.auth.application.service.AuthService;
import com.alexguedes.platform.shared.dto.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;
    private final ApiKeyService apiKeyService;

    public AuthController(AuthService authService, ApiKeyService apiKeyService) {
        this.authService = authService;
        this.apiKeyService = apiKeyService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request.username(), request.password())
                .map(response -> ResponseEntity.ok(ApiResponse.ok("authenticated", response)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("invalid credentials")));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponse>> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("token refreshed", authService.refresh(request.refreshToken())));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@Valid @RequestBody LogoutRequest request) {
        authService.logout(request.refreshToken());
        return ResponseEntity.ok(ApiResponse.ok("logged out", null));
    }

    @PostMapping("/api-key")
    public ResponseEntity<ApiResponse<ApiKeyResponse>> apiKey(
            @RequestHeader(name = "Authorization", required = false) String authorization,
            @Valid @RequestBody(required = false) ApiKeyRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("api key generated", apiKeyService.create(authorization, request)));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegisterResponse>> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("user registered", authService.register(request)));
    }
}
