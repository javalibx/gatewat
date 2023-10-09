package com.ubunx.gateway.security;

import java.util.List;
import java.util.Map;

public interface ResourceService {

    /**
     * 获取所有资源对应的角色
     *
     * @return 资源对应的角色
     */
    Map<String, List<String>> get();


}
