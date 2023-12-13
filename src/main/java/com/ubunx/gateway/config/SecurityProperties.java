package com.ubunx.gateway.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

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

    }
}
