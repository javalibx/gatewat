package com.ubunx.gateway.config;


import lombok.Data;
import lombok.NonNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.lang.Nullable;
import org.springframework.util.CollectionUtils;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.List;
import java.util.Objects;

@Data
@ConfigurationProperties(prefix = "security")
public class SecurityProperties {

    private Jwt jwt;

    private Path path;

    @Data
    public static class Jwt {
        private RSAPublicKey publicKey;

        private RSAPrivateKey privateKey;
    }

    @Data
    public static class Path {
        /**
         * 匿名路由
         */
        @Nullable
        private List<String> anonymous;

        /**
         * 用户访问路由白名单
         */
        @Nullable
        private List<String> whitelist;

        /**
         * 任务脚本路由列表
         */
        @Nullable
        private List<String> scripts;

        /**
         * 任务脚本路由列表，所对应的ip地址
         * 需要使用 IP/网络掩码的特定 IP 地址或范围（例如192.168.1.0/24）
         */
        @Nullable
        private String scriptIpaddress;

        public String[] getAnonymous() {
            if (CollectionUtils.isEmpty(anonymous)) {
                return null;
            }
            return anonymous.toArray(String[]::new);
        }

        public String[] getScripts() {
            if (CollectionUtils.isEmpty(scripts)) {
                return null;
            }
            return scripts.toArray(String[]::new);
        }

        public boolean isInWhitelist(String path) {
            if (CollectionUtils.isEmpty(whitelist)) {
                return false;
            }
            return whitelist.contains(path);
        }
    }
}
