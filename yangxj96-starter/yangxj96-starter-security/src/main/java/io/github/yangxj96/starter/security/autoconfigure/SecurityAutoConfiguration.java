/*****************************
 * Copyright (c) 2021 - 2023
 * author:yangxj96
 * email :yangxj96@gmail.com
 * date  :2023-01-07 00:11:06
 * Copyright (c) 2021 - 2023
 ****************************/

package io.github.yangxj96.starter.security.autoconfigure;

import cn.hutool.extra.spring.SpringUtil;
import io.github.yangxj96.starter.security.bean.StoreType;
import io.github.yangxj96.starter.security.exception.handle.AccessDeniedHandlerImpl;
import io.github.yangxj96.starter.security.exception.handle.AuthenticationEntryPointImpl;
import io.github.yangxj96.starter.security.filter.UserAuthorizationFilter;
import io.github.yangxj96.starter.security.properties.SecurityProperties;
import io.github.yangxj96.starter.security.store.TokenStore;
import io.github.yangxj96.starter.security.store.impl.JdbcTokenStore;
import io.github.yangxj96.starter.security.store.impl.RedisTokenStore;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * security??????
 *
 * @author yangxj96
 * @version 1.0
 * @date 2023-01-07 00:14
 */
@Slf4j
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true)
@ConditionalOnProperty(name = "yangxj96.security.enable", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(SecurityProperties.class)
public class SecurityAutoConfiguration {

    private static final String LOG_PREFIX = "[autoconfig-security] ";
    private final SecurityProperties properties;
    @Resource
    private AuthenticationConfiguration authenticationConfiguration;

    public SecurityAutoConfiguration(@Autowired SecurityProperties properties) {
        this.properties = properties;
    }

    /**
     * ???????????????
     *
     * @return {@link PasswordEncoder}
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        log.info("{}????????????????????????", LOG_PREFIX);
        return new BCryptPasswordEncoder();
    }


    /**
     * security?????????????????????
     *
     * @return {@link SecurityFilterChain }
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        log.info("{}?????????security????????????", LOG_PREFIX);
        http
                .securityContext(security -> security.requireExplicitSave(true));

        // ?????? cors csrf form httpBasic
        http
                .cors().disable()
                .csrf().disable()
                .httpBasic().disable()
                .formLogin().disable();

        // ?????????????????????????????????
        http
                .exceptionHandling()
                .authenticationEntryPoint(new AuthenticationEntryPointImpl())
                .accessDeniedHandler(new AccessDeniedHandlerImpl());

        // ??????????????????,????????????????????????????????????????????????
        http
                .authorizeHttpRequests()
                .anyRequest()
                .permitAll();

        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        TokenStore store;
        if (properties.getStoreType() == StoreType.JDBC) {
            log.debug("{},store??????jdbc", LOG_PREFIX);
            JdbcTemplate jdbcTemplate = SpringUtil.getBean(JdbcTemplate.class);
            store = new JdbcTokenStore(jdbcTemplate);
        } else {
            log.debug("{},store??????redis", LOG_PREFIX);
            RedisTemplate<String, Object> redisTemplate = SpringUtil.getBean("securityRedisTemplate");
            RedisTemplate<String, byte[]> bytesRedisTemplate = SpringUtil.getBean("securityBytesRedisTemplate");
            store = new RedisTokenStore(redisTemplate, bytesRedisTemplate);
        }

        http
                .addFilterAt(new UserAuthorizationFilter(authenticationConfiguration.getAuthenticationManager(), store), UsernamePasswordAuthenticationFilter.class)
        ;

        return http.build();
    }

    /**
     * ????????????
     *
     * @return {@link RoleHierarchy}
     */
    @Bean
    public RoleHierarchy roleHierarchy() {
        log.info("{}?????????????????????", LOG_PREFIX);
        RoleHierarchyImpl hierarchy = new RoleHierarchyImpl();
        hierarchy.setHierarchy("ROLE_ADMIN > ROLE_USER");
        return hierarchy;
    }

    /**
     * ???????????????
     *
     * @return {@link AuthenticationManager}
     * @throws Exception e
     */
    @Bean
    public AuthenticationManager authenticationManager() throws Exception {
        log.info("{}?????????????????????", LOG_PREFIX);
        return authenticationConfiguration.getAuthenticationManager();
    }


}
