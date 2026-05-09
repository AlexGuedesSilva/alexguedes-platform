package com.alexguedes.platform.validate.security;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class RedisReplayAttackGuard implements ReplayAttackGuard {
    private final StringRedisTemplate redisTemplate;

    public RedisReplayAttackGuard(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean reserve(String clientKey, String nonce, Duration ttl) {
        String key = "nonce:" + clientKey + ":" + nonce;
        Boolean stored = redisTemplate.opsForValue().setIfAbsent(key, "1", ttl);
        return Boolean.TRUE.equals(stored);
    }
}
