package com.alexguedes.platform.auth.application.service;

import com.alexguedes.platform.auth.api.dto.RegisterRequest;
import com.alexguedes.platform.auth.api.dto.RegisterResponse;
import com.alexguedes.platform.auth.api.dto.TokenResponse;
import com.alexguedes.platform.auth.application.exception.DuplicateResourceException;
import com.alexguedes.platform.auth.domain.model.UserEntity;
import com.alexguedes.platform.auth.infrastructure.repository.UserRepository;
import com.alexguedes.platform.auth.security.JwtService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            JwtService jwtService,
            UserRepository userRepository,
            RefreshTokenService refreshTokenService,
            BCryptPasswordEncoder passwordEncoder
    ) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.refreshTokenService = refreshTokenService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        String username = normalizeUsername(request.username());
        String email = normalizeEmail(request.email());

        if (userRepository.existsByUsername(username)) {
            throw new DuplicateResourceException("username already exists");
        }

        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("email already exists");
        }

        long usersCount = userRepository.count();

        String role = usersCount == 0
                ? "ADMIN"
                : "USER";

        UserEntity user = new UserEntity();

        user.setId(UUID.randomUUID());
        user.setUsername(username);
        user.setEmail(email);

        user.setPasswordHash(
                passwordEncoder.encode(request.password())
        );

        user.setRole(role);
        user.setEnabled(true);
        user.setCreatedAt(Instant.now());

        try {
            userRepository.save(user);
        } catch (DataIntegrityViolationException ex) {
            throw new DuplicateResourceException("username or email already exists");
        }

        return toRegisterResponse(user);
    }

    @Transactional
    public Optional<TokenResponse> login(String username, String password) {
        return userRepository.findByUsername(normalizeUsername(username))
                .filter(user -> Boolean.TRUE.equals(user.getEnabled()))
                .filter(user -> passwordEncoder.matches(password, user.getPasswordHash()))
                .map(this::createTokenResponse);
    }

    @Transactional
    public TokenResponse refresh(String refreshToken) {
        RefreshTokenService.RefreshTokenRotation rotation = refreshTokenService.rotate(refreshToken);
        return createTokenResponse(rotation.user(), rotation.refreshToken());
    }

    @Transactional
    public void logout(String refreshToken) {
        refreshTokenService.revoke(refreshToken);
    }

    private RegisterResponse toRegisterResponse(UserEntity user) {
        return new RegisterResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                user.getCreatedAt()
        );
    }

    private String normalizeUsername(String username) {
        return username == null ? "" : username.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    private TokenResponse createTokenResponse(UserEntity user) {
        return createTokenResponse(user, refreshTokenService.issue(user));
    }

    private TokenResponse createTokenResponse(UserEntity user, String refreshToken) {
        return new TokenResponse(
                jwtService.generateToken(user),
                refreshToken,
                "Bearer",
                jwtService.expiresInSeconds()
        );
    }
}
