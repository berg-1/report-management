package com.neo.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 自定义配置
 *
 * @author Berg
 */
@Configuration
public class MyBatisPlusConfig {
    /**
     * 配置MyBatis 数据库类型 分页需要
     *
     * @return interceptor
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        PaginationInnerInterceptor page = new PaginationInnerInterceptor();
        // 分页拦截器
        page.setOverflow(true);
        page.setMaxLimit(500L);
        interceptor.addInnerInterceptor(page);
        return interceptor;
    }

}
