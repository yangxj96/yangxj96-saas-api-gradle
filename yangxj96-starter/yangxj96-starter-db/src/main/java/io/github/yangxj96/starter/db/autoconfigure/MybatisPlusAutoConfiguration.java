/*****************************
 * Copyright (c) 2021 - 2023
 * author:yangxj96
 * email :yangxj96@gmail.com
 * date  :2023-01-07 00:11:06
 * Copyright (c) 2021 - 2023
 ****************************/

package io.github.yangxj96.starter.db.autoconfigure;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import io.github.yangxj96.starter.db.configure.jdbc.MetaObjectHandlerImpl;
import io.github.yangxj96.starter.db.properties.DBProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * mybatis的自动配置类
 *
 * @author yangxj96
 * @version 1.0
 * @date 2023-01-07 00:14
 */
@Slf4j
@ConditionalOnProperty(name = "yangxj96.db.jdbc-enable", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(DBProperties.class)
public class MybatisPlusAutoConfiguration {

    private static final String LOG_PREFIX = "[autoconfig-mybatis plus] ";
    private final DBProperties properties;

    public MybatisPlusAutoConfiguration(DBProperties properties) {
        this.properties = properties;
    }


    @Bean
    public MetaObjectHandler metaObjectHandler() {
        log.debug("{}元对象字段填充控制器", LOG_PREFIX);
        return new MetaObjectHandlerImpl();
    }

    /**
     * MyBatis Plus插件
     * <p>一缓和二缓遵循mybatis的规则</p>
     *
     * @return {@link MybatisPlusInterceptor} MyBatis Plus拦截器
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        log.debug(LOG_PREFIX + "载入MybatisPlusInterceptor");
        // 分页插件
        PaginationInnerInterceptor pageInterceptor = new PaginationInnerInterceptor();
        pageInterceptor.setOverflow(true);
        pageInterceptor.setMaxLimit(500L);
        pageInterceptor.setDbType(DbType.POSTGRE_SQL);
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(pageInterceptor);
        // 针对 update 和 delete 语句 作用: 阻止恶意的全表更新删除
        interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());
        return interceptor;
    }

}
