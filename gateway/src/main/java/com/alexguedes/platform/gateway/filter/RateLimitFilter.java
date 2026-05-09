package com.alexguedes.platform.gateway.filter;

import com.alexguedes.platform.gateway.config.Bucket4jConfig.RateLimitProperties;
import io.github.bucket4j.BucketConfiguration;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;

@Component
public class RateLimitFilter implements GlobalFilter, Ordered {
    private final ReactiveStringRedisTemplate redisTemplate;
    private final RateLimitProperties properties;
    private final BucketConfiguration bucketConfiguration;

    public RateLimitFilter(
            ReactiveStringRedisTemplate redisTemplate,
            RateLimitProperties properties,
            BucketConfiguration bucketConfiguration
    ) {
        this.redisTemplate = redisTemplate;
        this.properties = properties;
        this.bucketConfiguration = bucketConfiguration;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String clientIp = clientIp(exchange);
        String key = "rate_limit:" + clientIp;

        return redisTemplate.opsForValue()
                .increment(key)
                .flatMap(current -> refreshWindowIfNeeded(key, current).thenReturn(current))
                .flatMap(current -> {
                    long capacity = bucketConfiguration.getBandwidths()[0].getCapacity();
                    exchange.getResponse().getHeaders().add("X-RateLimit-Limit", String.valueOf(capacity));
                    exchange.getResponse().getHeaders().add("X-RateLimit-Remaining", String.valueOf(Math.max(0, capacity - current)));

                    if (current > properties.getCapacity()) {
                        exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                        return exchange.getResponse().setComplete();
                    }

                    return chain.filter(exchange);
                });
    }

    private Mono<Boolean> refreshWindowIfNeeded(String key, Long current) {
        if (current != null && current == 1L) {
            return redisTemplate.expire(key, properties.getWindow());
        }
        return Mono.just(false);
    }

    private String clientIp(ServerWebExchange exchange) {
        String forwardedFor = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }

        InetSocketAddress address = exchange.getRequest().getRemoteAddress();
        return address == null ? "unknown" : address.getAddress().getHostAddress();
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
