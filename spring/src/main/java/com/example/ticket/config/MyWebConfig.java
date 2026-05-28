package com.example.ticket.config;

import com.example.ticket.interceptor.JwtInterceptor;
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

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry){
        // 自定义图片资源映射
        registry.addResourceHandler("/static/imgs/**")
                .addResourceLocations("file:D:/test-imgs/xw_imgs/");
        
        // 忽略 favicon.ico 等静态资源请求
        registry.addResourceHandler("/favicon.ico")
                .addResourceLocations("classpath:/static/");
    }

//    @Override
//    public void addInterceptors(InterceptorRegistry registry) {
//        registry.addInterceptor(jwtInterceptor)
//                .addPathPatterns("/api/v1/**")
//                .excludePathPatterns(
//                        "/api/v1/g/**",
//                        "/api/v1/login",
//                        "/api/v1/reg",
//                        "/api/v1/admin/reg",
//                        "/api/v1/index"
//                );
//    }
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