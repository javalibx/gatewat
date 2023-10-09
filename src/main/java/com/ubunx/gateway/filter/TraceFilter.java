package com.ubunx.gateway.filter;

import com.javalibx.component.common.context.TraceContext;
import com.javalibx.component.common.support.constant.RequestHeaders;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;


/**
 * 链路信息过滤器，将客户端、Trace信息透传
 */
@Slf4j
@Component
public class TraceFilter implements GlobalFilter, Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String clientId = exchange.getRequest().getHeaders().getFirst(RequestHeaders.CLIENT_ID);
        String traceId = exchange.getRequest().getHeaders().getFirst(RequestHeaders.TRACE_ID);
        if (!StringUtils.hasText(traceId)) {
            traceId = UUID.randomUUID().toString().toLowerCase();
        }
        AtomicReference<String> finalTraceId = new AtomicReference<>(traceId);
        MDC.put(TraceContext.TRACE_ID, finalTraceId.get());
        ServerHttpRequest request = exchange.getRequest().mutate()
                .headers(headers -> {
                    headers.set(RequestHeaders.CLIENT_ID, clientId);
                    headers.set(RequestHeaders.TRACE_ID, finalTraceId.get());
                })
                .build();
        exchange = exchange.mutate().request(request).build();

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }
}
