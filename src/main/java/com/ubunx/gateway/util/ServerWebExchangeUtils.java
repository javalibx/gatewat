package com.ubunx.gateway.util;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ServerWebExchange;

public class ServerWebExchangeUtils {
    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private ServerWebExchangeUtils() {
    }

    /**
     * Gets the current {@link ServerWebExchange} instance for the thread.
     *
     * @return the current server web exchange, or {@code null} if there is no current exchange
     */
    public static ServerWebExchange getCurrentServerWebExchange() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes instanceof ServletRequestAttributes) {
            return (ServerWebExchange) attributes.getAttribute(ServerWebExchange.class.getName(), RequestAttributes.SCOPE_REQUEST);
        }
        return null;
    }
}
