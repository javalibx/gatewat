package com.ubunx.gateway.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.WebProperties.Resources;
import org.springframework.boot.autoconfigure.web.reactive.error.DefaultErrorWebExceptionHandler;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.server.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 自定义网关异常处理器
 */
@Slf4j
public class GatewayErrorWebExceptionHandler extends DefaultErrorWebExceptionHandler {

    public GatewayErrorWebExceptionHandler(ErrorAttributes errorAttributes, Resources resources,
                                           ErrorProperties errorProperties, ApplicationContext applicationContext) {
        super(errorAttributes, resources, errorProperties, applicationContext);
    }

    @Override
    protected Map<String, Object> getErrorAttributes(ServerRequest request, ErrorAttributeOptions options) {
        Throwable error = super.getError(request);
        Map<String, Object> errorAttributes = new HashMap<>();
        errorAttributes.put("success", false);
        errorAttributes.put("code", GatewayError.COMMON.getCode());
        String message = StringUtils.hasText(error.getMessage()) ? error.getMessage() : GatewayError.COMMON.getMessage();
        errorAttributes.put("message", message);
        errorAttributes.put("data", null);
        return errorAttributes;
    }

    // 指定响应处理方法为JSON处理的方法
    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
        return RouterFunctions.route(RequestPredicates.all(), this::renderErrorResponse);
    }

    @Override
    protected int getHttpStatus(Map<String, Object> errorAttributes) {
        return HttpStatus.OK.value();
    }
}
