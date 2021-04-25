package com.neo.config;

import com.neo.interceptor.LoginInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.annotation.WebListener;

/**
 * @author Berg
 */
@Configuration
@WebListener
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 包括静态资源,所有请求都拦截
        registry.addInterceptor(new LoginInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns("/", "/favicon.ico", "/login", "/css/**",
                        "/js/**", "/fonts/**", "/images/**", "/error", "/instances");
    }
}
