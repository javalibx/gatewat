package com.ubunx.gateway.config;


import com.javalibx.component.common.support.constant.SecurityConstants;
import com.javalibx.component.security.ResourceService;
import com.javalibx.component.security.server.JwtAuthenticationEntryPoint;
import com.javalibx.component.security.server.authorization.ResourceAccessDeniedHandler;
import com.javalibx.component.security.server.authorization.ResourceAuthorizationManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Slf4j
@Configuration
@EnableWebFluxSecurity
@EnableConfigurationProperties(SecurityProperties.class)
public class SecurityConfig {

    private final SecurityProperties properties;

    public SecurityConfig(SecurityProperties properties) {
        this.properties = properties;
    }

    @Bean
    ResourceAuthorizationManager resourceAuthorizationManager(ResourceService resourceService) {
        return new ResourceAuthorizationManager(properties.getPath().getWhitelist(), resourceService);
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity httpSecurity,
                                                         ResourceAuthorizationManager resourceAuthorizationManager) {
        log.info("[GSC] 加载Security配置信息...");
        httpSecurity
                .authorizeExchange(exchange -> {
                    exchange.pathMatchers(HttpMethod.OPTIONS).permitAll();
                    // 匿名路径
                    if (Objects.nonNull(properties.getPath().getAnonymous())) {
                        exchange.pathMatchers(properties.getPath().getAnonymous()).permitAll();
                    }
                    // 脚本
                    if (Objects.nonNull(properties.getPath().getScripts())) {
                        ServerHttpSecurity.AuthorizeExchangeSpec.Access access = exchange.pathMatchers(properties.getPath().getScripts());
                        if (StringUtils.hasText(properties.getPath().getScriptIpaddress())) {
                            access.hasIpAddress(properties.getPath().getScriptIpaddress());
                        } else {
                            access.permitAll();
                        }
                    }
                    exchange.anyExchange().access(resourceAuthorizationManager);
                })
                .oauth2ResourceServer(oars -> oars.jwt(jwt -> {
                    // 将jwt信息转换成JwtAuthenticationToken对象
                    jwt.jwtAuthenticationConverter(grantedAuthoritiesExtractor());
                    jwt.jwtDecoder(reactiveJwtDecoder());
                }))
                .exceptionHandling(es -> {
                    // 还没有认证时发生认证异常，比如token过期，token不合法
                    es.authenticationEntryPoint(new JwtAuthenticationEntryPoint());
                    // 认证后没有权限操作
                    es.accessDeniedHandler(new ResourceAccessDeniedHandler());
                })
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .cors(ServerHttpSecurity.CorsSpec::disable)
                .csrf(ServerHttpSecurity.CsrfSpec::disable);

        return httpSecurity.build();
    }

    @Bean
    ReactiveJwtDecoder reactiveJwtDecoder() {
        return NimbusReactiveJwtDecoder.withPublicKey(properties.getJwt().getPublicKey()).build();
    }

    Converter<Jwt, Mono<AbstractAuthenticationToken>> grantedAuthoritiesExtractor() {
        JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();
        // 去掉 SCOPE_ 的前缀
        authoritiesConverter.setAuthorityPrefix(SecurityConstants.AUTHORITY_PREFIX);
        // 从 jwt claim 中那个字段获取权限，模式是从 scope 或 scp 字段中获取
        authoritiesConverter.setAuthoritiesClaimName(SecurityConstants.AUTHORITY_CLAIM_NAME);

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);

        return new ReactiveJwtAuthenticationConverterAdapter(jwtAuthenticationConverter);
    }
}
