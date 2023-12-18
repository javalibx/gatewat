package com.ubunx.gateway.security.authorization;

import com.javalibx.component.common.dto.response.ApiResponse;
import com.ubunx.gateway.exception.GatewayError;
import com.ubunx.gateway.util.ServerResponseUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 资源权限受限
 */
@Slf4j
public class ResourceAccessDeniedHandler implements ServerAccessDeniedHandler {
    @Override
    public Mono<Void> handle(ServerWebExchange exchange, AccessDeniedException denied) {
        log.error("[Security] 该资源权限受限", denied);
        return ServerResponseUtils.failure(exchange, ApiResponse.failure(GatewayError.AUTHORIZATION_ERROR));
    }
}