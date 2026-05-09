package com.alexguedes.platform.gateway.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.Refill;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class Bucket4jConfig {
    @Bean
    @ConfigurationProperties(prefix = "app.rate-limit")
    RateLimitProperties rateLimitProperties() {
        return new RateLimitProperties();
    }

    @Bean
    BucketConfiguration bucketConfiguration(RateLimitProperties properties) {
        Bandwidth limit = Bandwidth.classic(
                properties.getCapacity(),
                Refill.intervally(properties.getCapacity(), properties.getWindow())
        );
        return BucketConfiguration.builder()
                .addLimit(limit)
                .build();
    }

    public static class RateLimitProperties {
        private long capacity = 60;
        private Duration window = Duration.ofMinutes(1);

        public long getCapacity() {
            return capacity;
        }

        public void setCapacity(long capacity) {
            this.capacity = capacity;
        }

        public Duration getWindow() {
            return window;
        }

        public void setWindow(Duration window) {
            this.window = window;
        }
    }
}
