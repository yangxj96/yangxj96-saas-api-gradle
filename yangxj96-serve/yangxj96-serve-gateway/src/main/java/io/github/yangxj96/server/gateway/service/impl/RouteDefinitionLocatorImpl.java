package io.github.yangxj96.server.gateway.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.yangxj96.constant.SystemRedisKey;
import io.github.yangxj96.server.gateway.service.SysRouteService;
import io.github.yangxj96.server.gateway.utils.RouteUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class RouteDefinitionLocatorImpl implements RouteDefinitionLocator {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private SysRouteService routeService;
    @Resource
    private ObjectMapper om;

    @Override
    public Flux<RouteDefinition> getRouteDefinitions() {
        List<RouteDefinition> routes = new ArrayList<>();
        List<Object> values = redisTemplate.opsForHash().values(SystemRedisKey.SYSTEM_GATEWAY_REDIS_KEY);
        if (!values.isEmpty()) {
            values.forEach(definition -> {
                try {
                    routes.add(om.convertValue(om.valueToTree(definition), RouteDefinition.class));
                } catch (Exception e) {
                    log.error("转换出错:" + e.getMessage());
                    e.printStackTrace();
                }
            });
        } else {
            routeService.list(new LambdaQueryWrapper<>()).forEach(item -> {
                RouteDefinition definition = RouteUtil.assembleRouteDefinition(item);
                // 放到redis缓存中
                redisTemplate.opsForHash().putIfAbsent(SystemRedisKey.SYSTEM_GATEWAY_REDIS_KEY, definition.getId(), definition);
                routes.add(definition);
            });
        }
        return Flux.fromIterable(routes);
    }
}
