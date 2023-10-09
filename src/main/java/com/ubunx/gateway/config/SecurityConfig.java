package com.ubunx.gateway.config;


import com.ubunx.gateway.security.JwtAuthenticationEntryPoint;
import com.ubunx.gateway.security.ResourceService;
import com.ubunx.gateway.security.authorization.ResourceAccessDeniedHandler;
import com.ubunx.gateway.security.authorization.ResourceAuthorizationManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.oauth2.server.resource.web.server.ServerBearerTokenAuthenticationConverter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

@Slf4j
@EnableWebFluxSecurity
public class SecurityConfig {

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
                // 将jwt信息转换成JwtAuthenticationToken对象
                .jwtAuthenticationConverter(grantedAuthoritiesExtractor())
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
                        .pathMatchers("/auth/**", "/pub/**").permitAll()
                        .pathMatchers(HttpMethod.OPTIONS).permitAll()
                        .anyExchange()
                        .access(resourceAuthorizationManager)
                )
                .formLogin().disable()
                .httpBasic().disable()
                .httpBasic().disable()
                .cors().disable();
        // 认证后将认证透传

        return httpSecurity.build();
    }

    Converter<Jwt, Mono<AbstractAuthenticationToken>> grantedAuthoritiesExtractor() {
        JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();
        // 去掉 SCOPE_ 的前缀
        authoritiesConverter.setAuthorityPrefix("");
        // 从 jwt claim 中那个字段获取权限，模式是从 scope 或 scp 字段中获取
        authoritiesConverter.setAuthoritiesClaimName("scp");

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);

        return new ReactiveJwtAuthenticationConverterAdapter(jwtAuthenticationConverter);
    }
}
