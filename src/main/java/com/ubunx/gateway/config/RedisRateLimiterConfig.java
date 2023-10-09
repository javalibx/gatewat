package com.ubunx.gateway.config;

import com.javalibx.component.common.support.constant.RequestHeaders;
import com.ubunx.gateway.util.ServerRequestUtils;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

import java.util.Objects;

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

    @Bean("userIdResolver")
    public KeyResolver userIdResolver() {
        return exchange -> {
            String userId = exchange.getRequest().getHeaders().getFirst(RequestHeaders.AUTH_ID);
            if (Objects.isNull(userId)) {
                return null;
            }
            return Mono.just(userId);
        };
    }
}
