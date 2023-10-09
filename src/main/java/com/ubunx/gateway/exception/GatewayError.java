package com.ubunx.gateway.exception;


import com.javalibx.component.common.exception.Error;
import lombok.Getter;

@Getter
public enum GatewayError implements Error {
    COMMON("G00001", "网关内部异常"),
    UC_FALLBACK("U00001", "UC 服务暂不可用，请稍后重试..."),
    HR_ERROR("H00001", "HR 服务暂不可用，请稍后重试..."),
    TC_ERROR("T00001", "TC 服务暂不可用，请稍后重试..."),
    IM_ERROR("I00001", "IM 服务暂不可用，请稍后重试...");

    private final String code;
    private final String message;

    GatewayError(String code, String message) {
        this.code = code;
        this.message = message;
    }

}
