package com.ubunx.gateway.config;


import com.ubunx.gateway.filter.AuthFilter;
import com.ubunx.gateway.security.JwtAuthenticationEntryPoint;
import com.ubunx.gateway.security.ResourceService;
import com.ubunx.gateway.security.authorization.ResourceAccessDeniedHandler;
import com.ubunx.gateway.security.authorization.ResourceAuthorizationManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.web.server.ServerBearerTokenAuthenticationConverter;
import org.springframework.security.web.server.SecurityWebFilterChain;

import javax.crypto.SecretKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

@Slf4j
@EnableWebFluxSecurity
public class SecurityConfig {

    @Value("${jwt.public.key}")
    RSAPublicKey key;

    @Value("${jwt.private.key}")
    RSAPrivateKey privy;


    @Bean
    ResourceAuthorizationManager resourceAuthorizationManager(ResourceService resourceService) {
        return new ResourceAuthorizationManager(resourceService);
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity httpSecurity,
                                                         ResourceAuthorizationManager resourceAuthorizationManager) {
        log.info("[GatewaySecurity] 加载Security配置信息...");
        httpSecurity
                .oauth2ResourceServer()
                .jwt()
//                .jwtAuthenticationConverter()
                .jwtDecoder(jwtDecoder())
                .and()
                // 将token转换成一个认证对象
                .bearerTokenConverter(new ServerBearerTokenAuthenticationConverter())
                // 还没有认证时发生认证异常，比如token过期，token不合法
                .authenticationEntryPoint(new JwtAuthenticationEntryPoint())
                // 认证后没有权限操作
                .accessDeniedHandler(new ResourceAccessDeniedHandler())
                .and()
                // 请求拦截处理
                .authorizeExchange(exchange -> exchange
                        .pathMatchers("/auth/**").permitAll()
                        .pathMatchers(HttpMethod.OPTIONS).permitAll()
                        .anyExchange()
                        .access(resourceAuthorizationManager)
                )
                .formLogin().disable()
                .httpBasic().disable()
                .cors().disable()
                .addFilterAfter(new AuthFilter(), SecurityWebFiltersOrder.AUTHENTICATION);
                // 认证后将认证透传

        return httpSecurity.build();
    }

    @Bean
    ReactiveJwtDecoder jwtDecoder() {
        return NimbusReactiveJwtDecoder.withSecretKey(secretKey()).build();
    }

    SecretKey secretKey() {
        return new HmacKey("");
    }
}
