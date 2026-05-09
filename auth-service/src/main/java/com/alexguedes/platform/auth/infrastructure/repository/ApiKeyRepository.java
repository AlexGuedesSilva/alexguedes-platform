package com.alexguedes.platform.auth.infrastructure.repository;

import com.alexguedes.platform.auth.domain.model.ApiKeyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ApiKeyRepository extends JpaRepository<ApiKeyEntity, UUID> {

    Optional<ApiKeyEntity> findByKeyHashAndEnabledTrue(String keyHash);
}
