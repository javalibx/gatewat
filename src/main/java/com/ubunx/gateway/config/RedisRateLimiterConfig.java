package com.ubunx.gateway.config;

import com.ubunx.gateway.util.ServerRequestUtils;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

/**
 * 基于 Redis 限流策略配置
 */
@Configuration
public class RedisRateLimiterConfig {

    @Bean("ipResolver")
    public KeyResolver ipResolver() {
        return exchange -> {
            String ip = ServerRequestUtils.getClientIp(exchange.getRequest());
            return Mono.just(ip);
        };
    }
}
