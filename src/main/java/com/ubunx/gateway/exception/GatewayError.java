package com.ubunx.gateway.exception;


import com.javalibx.component.common.exception.Error;
import lombok.Getter;

@Getter
public enum GatewayError implements Error {
    COMMON("G00001", "网关内部异常"),
    AUTHENTICATION_ERROR("G01001", "认证失败..."),
    AUTHORIZATION_ERROR("G01002", "该资源权限受限..."),

    UC_FALLBACK("G02001", "UC 服务暂不可用，请稍后重试..."),

    HR_ERROR("G03001", "HR 服务暂不可用，请稍后重试..."),

    TC_ERROR("G04001", "TC 服务暂不可用，请稍后重试..."),

    IM_ERROR("G05001", "IM 服务暂不可用，请稍后重试...");

    private final String code;
    private final String message;

    GatewayError(String code, String message) {
        this.code = code;
        this.message = message;
    }

}
