package com.example.demo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static org.springframework.http.CacheControl.maxAge;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    // 添加拦截器
    @Autowired
    private LoginInterceptor loginInterceptor;
    @Autowired
    private AdminInterceptor adminInterceptor;
    @Autowired
    private UserInterceptor userInterceptor;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // 所有路径都支持CORS
                .allowedOriginPatterns("*") // 允许所有域名访问
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // 允许的请求方法
                .allowedHeaders("*") // 允许所有请求头
                .allowCredentials(true);// 允许携带认证信息
        // 添加以下配置以避免头部重复;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册登录拦截器，并设置拦截路径
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/**") // 拦截路径
                .excludePathPatterns(
                        // 可以在这里添加其他排除路径
                );

        registry.addInterceptor(adminInterceptor)
                .addPathPatterns("/admin/**") // 拦截路径
                .excludePathPatterns(
                        // 排除OPTIONS请求
                );

        registry.addInterceptor(userInterceptor)
                .addPathPatterns("/user/**") // 拦截路径
                .excludePathPatterns(
                        // 可以在这里添加其他排除路径，但推荐在拦截器内部处理OPTIONS
                );
    }
}
