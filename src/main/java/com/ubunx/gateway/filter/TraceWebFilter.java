package com.ubunx.gateway.filter;

import com.javalibx.component.common.context.TraceContext;
import com.javalibx.component.common.support.constant.RequestHeaders;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;


/**
 * 链路信息过滤器，将客户端、Trace信息透传
 */
@Slf4j
@Component
public class TraceWebFilter implements WebFilter, Ordered {

    @Override
    @NonNull
    public Mono<Void> filter(ServerWebExchange exchange, @NonNull WebFilterChain chain) {
        String clientId = exchange.getRequest().getHeaders().getFirst(RequestHeaders.CLIENT_ID);
        String traceId = exchange.getRequest().getHeaders().getFirst(RequestHeaders.TRACE_ID);
        if (!StringUtils.hasText(traceId)) {
            traceId = UUID.randomUUID().toString().toLowerCase();
        }
        AtomicReference<String> finalTraceId = new AtomicReference<>(traceId);
        ServerHttpRequest request = exchange.getRequest().mutate()
                .headers(headers -> {
                    headers.set(RequestHeaders.CLIENT_ID, clientId);
                    headers.set(RequestHeaders.TRACE_ID, finalTraceId.get());
                })
                .build();
        MDC.put(TraceContext.TRACE_ID, traceId);
        exchange = exchange.mutate().request(request).build();

        return chain.filter(exchange).doFinally(signalType -> {
            // 清除MDC信息以避免内存泄漏
            MDC.clear();
        });
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
