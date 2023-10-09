package com.ubunx.gateway.loadbalancer;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.Request;
import org.springframework.cloud.client.loadbalancer.RequestDataContext;
import org.springframework.cloud.client.loadbalancer.Response;
import org.springframework.cloud.loadbalancer.core.RandomLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import reactor.core.publisher.Mono;

import java.util.Map;

public class TokenLoadBalancer extends RandomLoadBalancer {
    /**
     * @param serviceInstanceListSupplierProvider a provider of
     *                                            {@link ServiceInstanceListSupplier} that will be used to get available instances
     * @param serviceId                           id of the service for which to choose an instance
     */
    public TokenLoadBalancer(ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider, String serviceId) {
        super(serviceInstanceListSupplierProvider, serviceId);
    }

    @Override
    public Mono<Response<ServiceInstance>> choose(Request request) {
        String token = extractTokenFromRequest(request);
        // 根据 Token 参数选择要路由的后端服务实例
        return Mono.empty();
    }


    private String extractTokenFromRequest(Request<?> request) {
        // 从请求中提取 Token 参数的逻辑
        RequestDataContext context = (RequestDataContext) request.getContext();
        Map<String, Object> attributes = context.getClientRequest().getAttributes();
        return null;
    }


}
