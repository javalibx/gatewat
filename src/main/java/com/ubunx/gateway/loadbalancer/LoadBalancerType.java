package com.ubunx.gateway.loadbalancer;

public enum LoadBalancerType {
    /**
     * 开发环境，优先自己的服务
     */
    DEV,

    /**
     * 轮询
     */
    ROUND_ROBIN,

    /**
     * 随机
     */
    RANDOM,

    /**
     * URL
     */
    TOKEN
}
