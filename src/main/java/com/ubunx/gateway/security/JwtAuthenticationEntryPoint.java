package com.ubunx.gateway.security;

import com.javalibx.component.common.dto.response.ApiResponse;
import com.ubunx.gateway.exception.GatewayError;
import com.ubunx.gateway.util.ServerResponseUtils;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 认证失败
 */
public class JwtAuthenticationEntryPoint implements ServerAuthenticationEntryPoint {
    @Override
    public Mono<Void> commence(ServerWebExchange exchange, AuthenticationException ex) {
        return ServerResponseUtils.failure(exchange, ApiResponse.failure(GatewayError.AUTHENTICATION_ERROR));
    }
}
