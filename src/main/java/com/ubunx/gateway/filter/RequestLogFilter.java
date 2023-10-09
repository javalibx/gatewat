package com.ubunx.gateway.filter;

import com.javalibx.component.common.dto.DTO;
import com.javalibx.component.common.support.constant.RequestHeaders;
import com.javalibx.component.common.util.JsonUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * 请求日志过滤器，将请求耗时、请求信息写入日志
 */
@Slf4j
@Component
public class RequestLogFilter implements GlobalFilter, Ordered {

    private static final String START_TIME = "startTime";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        exchange.getAttributes().put(START_TIME, System.currentTimeMillis());

        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            Long startTime = exchange.getAttribute(START_TIME);
            if (Objects.nonNull(startTime)) {
                Long executeTime = (System.currentTimeMillis() - startTime);
                String ip = exchange.getRequest().getHeaders().getFirst(RequestHeaders.CLIENT_IP);
                RequestLog requestLog = new RequestLog();
                requestLog.setExecuteTime(executeTime);
                requestLog.setIp(ip);
                requestLog.setMethod(exchange.getRequest().getMethodValue());
                requestLog.setUri(exchange.getRequest().getURI().getRawPath());
                requestLog.setQueries(exchange.getRequest().getQueryParams());
                log.info("[GatewayLog] 请求信息 - {}", requestLog);
            }
        }));
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    @Data
    @EqualsAndHashCode(callSuper = false)
    private static class RequestLog extends DTO {

        private Long executeTime;

        /**
         * IP地址
         */
        private String ip;

        /**
         * 请求类型
         */
        private String method;

        /**
         * URL
         */
        private String uri;

        /**
         * 请求参数
         */
        private Object queries;

        private Object body;

        @Override
        public String toString() {
            return JsonUtils.object2Json(this);
        }
    }
}
