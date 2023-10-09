package com.ubunx.gateway.config;

import com.ubunx.gateway.exception.GatewayErrorWebExceptionHandler;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.web.reactive.result.view.ViewResolver;

import java.util.List;

/**
 * 网关异常处理配置
 */
@Configuration
@EnableConfigurationProperties({ServerProperties.class, WebProperties.class})
public class ErrorHandlerConfig {
    private final ServerProperties serverProperties;

    private final ApplicationContext applicationContext;

    private final WebProperties webProperties;

    private final List<ViewResolver> viewResolvers;

    private final ServerCodecConfigurer serverCodecConfigurer;

    public ErrorHandlerConfig(ServerProperties serverProperties,
                              ApplicationContext applicationContext,
                              WebProperties webProperties,
                              List<ViewResolver> viewResolvers,
                              ServerCodecConfigurer serverCodecConfigurer) {
        this.serverProperties = serverProperties;
        this.applicationContext = applicationContext;
        this.webProperties = webProperties;
        this.viewResolvers = viewResolvers;
        this.serverCodecConfigurer = serverCodecConfigurer;
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public ErrorWebExceptionHandler errorWebExceptionHandler(ErrorAttributes errorAttributes) {
        GatewayErrorWebExceptionHandler exceptionHandler = new GatewayErrorWebExceptionHandler(
                errorAttributes,
                webProperties.getResources(),
                serverProperties.getError(),
                applicationContext);
        exceptionHandler.setViewResolvers(viewResolvers);
        exceptionHandler.setMessageWriters(serverCodecConfigurer.getWriters());
        exceptionHandler.setMessageReaders(serverCodecConfigurer.getReaders());
        return exceptionHandler;
    }
}
