package com.ubunx.gateway.security.authentication;

import com.javalibx.component.common.support.constant.RequestHeaders;
import com.ubunx.gateway.security.AuthenticatedUser;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Objects;

public class JwtAuthenticationSuccessHandler implements ServerAuthenticationSuccessHandler {
    @Override
    public Mono<Void> onAuthenticationSuccess(WebFilterExchange filterExchange, Authentication authentication) {
        ServerWebExchange exchange = filterExchange.getExchange();
        if (Objects.nonNull(authentication.getDetails())) {
            AuthenticatedUser user = (AuthenticatedUser) authentication.getDetails();
            ServerHttpRequest request = exchange.getRequest().mutate()
                    .headers(headers -> {
                        headers.set(RequestHeaders.AUTH_TYPE, user.getType());
                        headers.set(RequestHeaders.AUTH_ID, String.valueOf(user.getId()));
                    })
                    .build();
            exchange = exchange.mutate().request(request).build();
        }

        return filterExchange.getChain().filter(exchange);
    }
}
