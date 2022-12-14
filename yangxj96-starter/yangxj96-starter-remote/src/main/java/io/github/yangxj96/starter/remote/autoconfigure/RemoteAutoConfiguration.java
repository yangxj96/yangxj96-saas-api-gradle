/*****************************
 * Copyright (c) 2021 - 2023
 * author:yangxj96
 * email :yangxj96@gmail.com
 * date  :2023-01-07 00:11:06
 * Copyright (c) 2021 - 2023
 ****************************/

package io.github.yangxj96.starter.remote.autoconfigure;

import feign.*;
import feign.okhttp.OkHttpClient;
import io.github.yangxj96.starter.remote.configure.OkHttpLogInterceptor;
import io.github.yangxj96.starter.remote.properties.RemoteProperties;
import lombok.extern.slf4j.Slf4j;
import okhttp3.ConnectionPool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.client.loadbalancer.LoadBalancedRetryFactory;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.loadbalancer.*;
import org.springframework.cloud.openfeign.support.SpringMvcContract;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Import;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * 远程请求的openfeign配置
 *
 * @author yangxj96
 * @version 1.0
 * @date 2023-01-07 00:14
 */
@Slf4j
@Import(value = {FeignLoadBalancerAutoConfiguration.class})
@AutoConfiguration(before = FeignLoadBalancerAutoConfiguration.class)
@EnableFeignClients("io.github.yangxj96.starter.remote.clients")
@EnableConfigurationProperties(RemoteProperties.class)
public class RemoteAutoConfiguration {

    private static final String LOG_PREFIX = "[autoconfig-remote] ";

    /**
     * 项目配置文件
     */
    private final RemoteProperties properties;

    public RemoteAutoConfiguration(@Autowired RemoteProperties properties) {
        this.properties = properties;
    }

    /**
     * feign 请求重试配置
     *
     * @return Options
     */
    @Bean
    public Request.Options options() {
        return new Request.Options(
                // @formatter:off
                properties.getConnectTimeOut(), TimeUnit.MILLISECONDS,
                properties.getReadTimeOut(), TimeUnit.MILLISECONDS,
                true);
                // @formatter:on
    }

    /**
     * feign 重试机制
     *
     * @return 重试机制
     */
    @Bean
    public feign.Retryer retryer() {
        return new Retryer.Default(100, SECONDS.toMillis(1), 2);
    }

    /**
     * @return SpringMvcContract
     */
    @Bean
    public Contract contract() {
        return new SpringMvcContract();
    }

    /**
     * feign 日志管理
     *
     * @return feign 日志
     */
    @Bean
    public Logger.Level level() {
        return properties.getLevel();
    }

    /**
     * 定义okhttp3客户端
     *
     * @return OkHttpClient
     */
    @Bean
    @LoadBalanced
    public okhttp3.OkHttpClient okHttpClient() {
        log.debug(LOG_PREFIX + "创建okhttp3客户端信息");
        return new okhttp3.OkHttpClient
                .Builder()
                .readTimeout(properties.getReadTimeOut(), TimeUnit.MILLISECONDS)
                .connectTimeout(properties.getConnectTimeOut(), TimeUnit.MILLISECONDS)
                .writeTimeout(properties.getWriteTimeout(), TimeUnit.MILLISECONDS)
                .connectionPool(new ConnectionPool())
                .addInterceptor(new OkHttpLogInterceptor())
                .build();
    }

    @Bean
    public Client feignRetryClient(
            LoadBalancerClient loadBalancerClient,
            okhttp3.OkHttpClient okHttpClient,
            LoadBalancedRetryFactory loadBalancedRetryFactory,
            LoadBalancerClientFactory loadBalancerClientFactory,
            List<LoadBalancerFeignRequestTransformer> transformers
    ) {
        log.debug(LOG_PREFIX + "可重试的feign");
        OkHttpClient delegate = new OkHttpClient(okHttpClient);
        return new RetryableFeignBlockingLoadBalancerClient(delegate, loadBalancerClient, loadBalancedRetryFactory,
                loadBalancerClientFactory, transformers);
    }

    /**
     * feign 客户端
     *
     * @return feign 客户端
     */
    @Bean
    @ConditionalOnMissingBean
    @Conditional(OnRetryNotEnabledCondition.class)
    public Client feignClient(okhttp3.OkHttpClient okHttpClient,
                              LoadBalancerClient loadBalancerClient,
                              LoadBalancerClientFactory loadBalancerClientFactory) {

        log.debug(LOG_PREFIX + "使用okhttp3作为底层");
        OkHttpClient delegate = new OkHttpClient(okHttpClient);
        return new FeignBlockingLoadBalancerClient(delegate, loadBalancerClient, loadBalancerClientFactory, Collections.emptyList());
    }

}
