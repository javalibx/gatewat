package com.ubunx.gateway.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.StringUtils;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Objects;

/**
 * 获取客户端真实IP
 */
@Slf4j
public class ServerRequestUtils {
    private static final String[] HEADER_IP_KEYWORDS = {
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "X-Real-IP"
    };

    private static final String UNKNOWN = "unknown";
    private static final String LOCALHOST = "127.0.0.1";

    private static final String SEPARATOR = ",";

    /**
     * 获取客户端真实IP
     *
     * @param request 请求
     * @return IP
     */
    public static String getClientIp(ServerHttpRequest request) {
        String ipAddress = null;
        try {
            HttpHeaders headers = request.getHeaders();
            // 获取代理 IP
            for (String header : HEADER_IP_KEYWORDS) {
                ipAddress = headers.getFirst(header);
                if (StringUtils.hasText(ipAddress) && !UNKNOWN.equalsIgnoreCase(ipAddress)) {
                    break;
                }
            }
            if (!StringUtils.hasText(ipAddress) || UNKNOWN.equalsIgnoreCase(ipAddress)) {
                InetSocketAddress address = request.getRemoteAddress();
                if (Objects.nonNull(address)) {
                    ipAddress = address.getAddress().getHostAddress();
                }
                if (LOCALHOST.equals(ipAddress)) {
                    // 根据网卡取本机配置的IP
                    InetAddress inet = null;
                    try {
                        inet = InetAddress.getLocalHost();
                    } catch (UnknownHostException e) {
                        log.error("[Gateway] 获取客户端 IP 异常", e);
                    }
                    if (Objects.nonNull(inet)) {
                        ipAddress = inet.getHostAddress();
                    }
                }
            }
            // 对于通过多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割
            //  "***.***.***.***"
            if (Objects.nonNull(ipAddress)) {
                ipAddress = ipAddress.split(SEPARATOR)[0].trim();
            }
        } catch (Exception ex) {
            log.error("[Gateway] 获取客户端 IP 异常", ex);
            ipAddress = "";
        }

        return Objects.isNull(ipAddress) ? "" : ipAddress;
    }
}
