package com.alexguedes.platform.validate.security;

import java.time.Duration;

public interface ReplayAttackGuard {
    boolean reserve(String clientKey, String nonce, Duration ttl);
}
