package com.example.ticket.config;

import com.example.ticket.interceptor.JwtInterceptor;
import com.example.ticket.interceptor.RoleInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MyWebConfig implements WebMvcConfigurer {
    @Autowired
    private JwtInterceptor jwtInterceptor;
    
    @Autowired
    private RoleInterceptor roleInterceptor;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry){
        // 自定义图片资源映射
        registry.addResourceHandler("/static/imgs/**")
                .addResourceLocations("file:D:/test-imgs/xw_imgs/");
        
        // 忽略 favicon.ico 等静态资源请求
        registry.addResourceHandler("/favicon.ico")
                .addResourceLocations("classpath:/static/");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 1. JWT拦截器（第一层：验证Token）
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/api/v1/**")
                .excludePathPatterns(
                        // 登录注册相关（无需Token）
                        "/api/v1/login",           // 用户登录
                        "/api/v1/reg",             // 用户注册
                        "/api/v1/admin/reg",       // 管理员注册
                        // 公开查询接口（以/g/开头的接口）
                        "/api/v1/g/**",
                        // 静态资源
                        "/static/**",
                        "/favicon.ico",
                        "/**"
                );
        
        // 2. Role拦截器（第二层：验证权限，只处理JWT已放行的请求）
        registry.addInterceptor(roleInterceptor)
                .addPathPatterns("/api/v1/**")
                .excludePathPatterns(
                        // 登录注册相关
                        "/api/v1/login",
                        "/api/v1/reg",
                        "/api/v1/admin/reg",
                        // 公开查询接口
                        "/api/v1/g/**",
                        // 静态资源
                        "/static/**",
                        "/favicon.ico"
                );
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*") // 修复：高版本必须用这个
                .allowedMethods("POST", "GET", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("*") // 允许所有请求头（更稳）
                .allowCredentials(true) // 支持 token 跨域
                .maxAge(3600);
    }
}