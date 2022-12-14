/*****************************
 * Copyright (c) 2021 - 2023
 * author:yangxj96
 * email :yangxj96@gmail.com
 * date  :2023-01-07 00:08:39
 * Copyright (c) 2021 - 2023
 ****************************/

package io.github.yangxj96.server.auth.configuration;

import io.github.yangxj96.starter.security.exception.handle.AccessDeniedHandlerImpl;
import io.github.yangxj96.starter.security.exception.handle.AuthenticationEntryPointImpl;
import io.github.yangxj96.starter.security.filter.UserAuthorizationFilter;
import io.github.yangxj96.starter.security.store.TokenStore;
import io.github.yangxj96.starter.security.store.impl.RedisTokenStore;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;


/**
 * Security ????????????
 *
 * @author yangxj96
 * @version 1.0
 * @date 2023-01-07 00:14
 */
@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true)
public class WebSecurityConfiguration {

    private static final String LOG_PREFIX = "[????????????] ";


    @Resource
    private UserDetailsService userDetailsService;

    @Resource
    private AuthenticationConfiguration authenticationConfiguration;

    @Resource(name = "securityRedisTemplate")
    private RedisTemplate<String, Object> redisTemplate;


    @Resource(name = "securityBytesRedisTemplate")
    private RedisTemplate<String, byte[]> bytesRedisTemplate;

    /**
     * ???????????????
     *
     * @return ???????????????
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        log.info("{}?????????????????????", LOG_PREFIX);
        return new BCryptPasswordEncoder();
    }

    /**
     * token store
     *
     * @return {@link RedisTokenStore}
     */
    @Bean
    public TokenStore tokenStore() {
        log.info("{}??????token????????????2", LOG_PREFIX);
        return new RedisTokenStore(redisTemplate, bytesRedisTemplate);
    }

    /**
     * ??????????????????
     */
    @Bean
    public RoleHierarchyImpl roleHierarchyImpl() {
        log.info("{}????????????????????????", LOG_PREFIX);
        RoleHierarchyImpl hierarchy = new RoleHierarchyImpl();
        hierarchy.setHierarchy("ROLE_ADMIN > ROLE_USER");
        return hierarchy;
    }


    /**
     * HttpSecurity ??????
     *
     * @param http the {@link HttpSecurity} to modify
     * @return security ????????????
     * @throws Exception e
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, TokenStore tokenStore) throws Exception {
        log.info("{}??????????????????", LOG_PREFIX);
        // ??????cors csrf httpBasic formLogin
        http.cors().disable()
                .csrf().disable()
                .httpBasic().disable()
                .formLogin().disable()
        ;

        // ????????????????????????,?????????????????????????????????
        http
                .authorizeHttpRequests()
                .anyRequest().permitAll()
        ;

        // ????????????????????????????????????
        http
                .exceptionHandling()
                .accessDeniedHandler(new AccessDeniedHandlerImpl())
                .authenticationEntryPoint(new AuthenticationEntryPointImpl())
        ;

        // ?????????session
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        // ??????????????????????????????
        http
                .addFilterAt(
                        new UserAuthorizationFilter(authenticationManager(), tokenStore),
                        UsernamePasswordAuthenticationFilter.class)
        ;

        // ??????
        http.userDetailsService(userDetailsService);
        return http.build();
    }


    @Bean
    public AuthenticationManager authenticationManager() throws Exception {
        log.info("{}?????????????????????", LOG_PREFIX);
        return authenticationConfiguration.getAuthenticationManager();
    }

    /**
     * ????????????
     *
     * @return {@link CorsConfigurationSource}
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        log.info("{}??????????????????", LOG_PREFIX);
        CorsConfiguration configuration = new CorsConfiguration();
        //?????????????????????????????????* ??????????????????????????????????????????????????????????????????????????????
        configuration.addAllowedOriginPattern("*");
        //??????????????????????????????
        configuration.addAllowedMethod("*");
        //???????????????????????????????????? Access-Control-Allow-Origin
        configuration.addAllowedHeader("*");
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }


}

