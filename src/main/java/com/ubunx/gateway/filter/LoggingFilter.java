package com.ubunx.gateway.filter;

import com.javalibx.component.common.dto.DTO;
import com.javalibx.component.common.support.constant.RequestHeaders;
import com.javalibx.component.common.util.JsonUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.AdaptCachedBodyGlobalFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 请求日志过滤器，将请求耗时、请求信息写入日志
 */
@Slf4j
@Component
public class LoggingFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 记录处理开始时间
        Instant start = Instant.now();
        String body;
        DataBuffer cachedBody = exchange.getAttribute(ServerWebExchangeUtils.CACHED_REQUEST_BODY_ATTR);
        if (Objects.nonNull(cachedBody) && cachedBody.readableByteCount() > 0) {
            body = toBufferString(cachedBody);
        } else {
            body = "";
        }
        return chain.filter(exchange).doFinally(st -> {
            // 记录处理结束时间
            Instant end = Instant.now();
            Duration between = Duration.between(start, end);
            String ip = exchange.getRequest().getHeaders().getFirst(RequestHeaders.CLIENT_IP);
            RequestLog requestLog = new RequestLog();
            requestLog.setExecuteTime(between.toMillis());
            requestLog.setIp(ip);
            requestLog.setMethod(exchange.getRequest().getMethod().name());
            requestLog.setUri(exchange.getRequest().getURI().getRawPath());
            requestLog.setQueries(exchange.getRequest().getQueryParams());
            requestLog.setBody(body);
            log.info("[Gateway] request info is {}", requestLog);
        });
    }

    /**
     * {@link AdaptCachedBodyGlobalFilter} 要在这个之后执行
     *
     * @return int
     */
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 1001;
    }

    private static String toBufferString(DataBuffer buffer) {
        byte[] bytes = new byte[buffer.readableByteCount()];
        buffer.read(bytes);
        String body = new String(bytes, StandardCharsets.UTF_8);
        return format(body);
    }

    /**
     * 去掉空格,换行和制表符
     *
     * @param text String
     * @return String
     */
    private static String format(String text) {
        if (StringUtils.hasText(text)) {
            Pattern p = Pattern.compile("\\s*|\t|\r|\n");
            Matcher m = p.matcher(text);
            return m.replaceAll("");
        }

        return text;
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
