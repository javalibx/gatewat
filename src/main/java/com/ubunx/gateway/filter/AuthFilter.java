package com.ubunx.gateway.filter;

import com.javalibx.component.common.support.constant.RequestHeaders;
import com.javalibx.component.common.support.constant.SecurityConstants;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class AuthFilter implements GlobalFilter, Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 从Spring Security上下文中获取JWT令牌中的信息
        return ReactiveSecurityContextHolder.getContext()
                .flatMap(securityContext -> {
                    Authentication authentication = securityContext.getAuthentication();
                    if (authentication instanceof JwtAuthenticationToken jat) {
                        Jwt jwt = jat.getToken();
                        // 在这里，你可以从JWT中提取需要的信息，并将其存储在请求头或请求属性中
                        ServerHttpRequest request = exchange.getRequest().mutate()
                                .headers(headers -> {
                                    headers.set(RequestHeaders.AUTH_ID, jwt.getClaim(SecurityConstants.AUTHORITY_AUTH_ID_NAME));
                                    headers.set(RequestHeaders.AUTH_TYPE, jwt.getClaim(SecurityConstants.AUTHORITY_AUTH_TYPE_NAME));
                                    headers.set(RequestHeaders.AUTH_SCOPE, jwt.getClaim(SecurityConstants.AUTHORITY_CLAIM_NAME));
                                })
                                .build();
                        ServerWebExchange updatedExchange = exchange.mutate().request(request).build();
                        return chain.filter(updatedExchange);
                    }
                    return chain.filter(exchange);
                });
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
