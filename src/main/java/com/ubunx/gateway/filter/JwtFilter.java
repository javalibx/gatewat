package com.ubunx.gateway.filter;

import com.javalibx.component.common.support.constant.RequestHeaders;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public class JwtFilter implements GlobalFilter, Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 从Spring Security上下文中获取JWT令牌中的信息
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken) {
            Jwt jwt = ((JwtAuthenticationToken) authentication).getToken();
            // 在这里，你可以从JWT中提取需要的信息，并将其存储在请求头或请求属性中
            ServerHttpRequest request = exchange.getRequest().mutate()
                    .headers(headers -> {
                        headers.set(RequestHeaders.AUTH_ID, jwt.getClaim("sub"));
                        headers.set(RequestHeaders.AUTH_TYPE, jwt.getClaim("subType"));
                        headers.set(RequestHeaders.AUTH_SCOPE, jwt.getClaim("scp"));
                    })
                    .build();
            exchange = exchange.mutate().request(request).build();
        }
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}