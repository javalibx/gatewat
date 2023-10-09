package com.ubunx.gateway.loadbalancer;

import com.ubunx.gateway.util.LocalMachineInfo;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.DefaultResponse;
import org.springframework.cloud.client.loadbalancer.EmptyResponse;
import org.springframework.cloud.client.loadbalancer.Request;
import org.springframework.cloud.client.loadbalancer.Response;
import org.springframework.cloud.loadbalancer.core.*;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;


public class DevLoadBalancer implements ReactorServiceInstanceLoadBalancer {

    private static final Log log = LogFactory.getLog(RoundRobinLoadBalancer.class);

    private final String serviceId;

    private final AtomicInteger position;

    private final LoadBalancerType type;

    private final ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider;

    protected DevLoadBalancer(String serviceId,
                              LoadBalancerType type,
                              ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider) {
        this.serviceId = serviceId;
        this.type = type;
        this.serviceInstanceListSupplierProvider = serviceInstanceListSupplierProvider;
        this.position = new AtomicInteger();
    }

    @Override
    public Mono<Response<ServiceInstance>> choose(Request request) {
        ServiceInstanceListSupplier supplier = serviceInstanceListSupplierProvider
                .getIfAvailable(NoopServiceInstanceListSupplier::new);
        return supplier.get(request).next()
                .map(serviceInstances -> processInstanceResponse(request, supplier, serviceInstances));
    }


    private Response<ServiceInstance> processInstanceResponse(Request<?> request,
                                                              ServiceInstanceListSupplier supplier,
                                                              List<ServiceInstance> serviceInstances) {
        Response<ServiceInstance> serviceInstanceResponse = getInstanceResponse(request, serviceInstances);

        if (supplier instanceof SelectedInstanceCallback && serviceInstanceResponse.hasServer()) {
            ((SelectedInstanceCallback) supplier).selectedServiceInstance(serviceInstanceResponse.getServer());
        }

        return serviceInstanceResponse;
    }

    private Response<ServiceInstance> getInstanceResponse(Request<?> request, List<ServiceInstance> instances) {
        if (instances.isEmpty()) {
            if (log.isWarnEnabled()) {
                log.warn("No servers available for service: " + serviceId);
            }
            return new EmptyResponse();
        }

        if (Objects.equals(type, LoadBalancerType.DEV)) {
            return getDevInstance(instances);
        } else if (Objects.equals(type, LoadBalancerType.RANDOM)) {
            return getRandomInstance(instances);
        }

        return getRoundRobinInstance(instances);
    }


    /**
     * 在开发环境的情况下优先获取本地服务，如果没有则轮询服务
     *
     * @param instances 所有实例
     * @return {@link Response }<{@link ServiceInstance}>
     */
    private Response<ServiceInstance> getDevInstance(List<ServiceInstance> instances) {
        // 获取本机ip
        String hostIp = LocalMachineInfo.getIpAddress();
        if (!StringUtils.hasText(hostIp)) {
            return getRoundRobinInstance(instances);
        }

        // 先取得和本地ip一样的服务，如果没有则按默认来取
        for (ServiceInstance instance : instances) {
            String host = instance.getHost();
            if (StringUtils.hasText(host) && Objects.equals(hostIp, host)) {
                return new DefaultResponse(instance);
            }
        }

        return getRoundRobinInstance(instances);
    }


    /**
     * 使用随机算法
     * 参考{link {@link org.springframework.cloud.loadbalancer.core.RandomLoadBalancer}}
     *
     * @param instances 所有实例
     * @return {@link Response }<{@link ServiceInstance}>
     */
    private Response<ServiceInstance> getRandomInstance(List<ServiceInstance> instances) {
        int index = ThreadLocalRandom.current().nextInt(instances.size());
        ServiceInstance instance = instances.get(index);
        return new DefaultResponse(instance);
    }


    private Response<ServiceInstance> getRoundRobinInstance(List<ServiceInstance> instances) {
        // 每一次计数器都自动+1，实现轮询的效果
        int pos = this.position.incrementAndGet() & Integer.MAX_VALUE;
        ServiceInstance instance = instances.get(pos % instances.size());
        return new DefaultResponse(instance);
    }
}
