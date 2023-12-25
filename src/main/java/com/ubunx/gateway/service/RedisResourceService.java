package com.ubunx.gateway.service;

import com.javalibx.component.security.ResourceService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RedisResourceService implements ResourceService {

    @Override
    public Map<String, List<String>> get() {
        HashMap<String, List<String>> map = new HashMap<>();
        ArrayList<String> roles = new ArrayList<>();
        roles.add("admin");
        map.put("/hr/info", roles);
        return map;
    }
}
