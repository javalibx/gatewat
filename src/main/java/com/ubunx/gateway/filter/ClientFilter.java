package com.ubunx.gateway.filter;

import com.javalibx.component.common.support.constant.RequestHeaders;
import com.ubunx.gateway.util.ServerRequestUtils;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 客户端IP过滤器，获取真实的客户端IP，并将IP透传
 */
@Component
public class ClientFilter implements GlobalFilter, Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String ip = ServerRequestUtils.getClientIp(exchange.getRequest());
        ServerHttpRequest request = exchange.getRequest().mutate()
                .header(RequestHeaders.CLIENT_ID, ip)
                .build();
        return chain.filter(exchange.mutate().request(request).build());
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 2;
    }
}
