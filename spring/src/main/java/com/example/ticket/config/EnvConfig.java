package com.example.ticket.config;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

/**
 * 环境变量配置类
 * 用于加载 .env 文件中的配置并设置到系统属性中
 */
@Configuration
public class EnvConfig {

    /**
     * 在 Spring 容器初始化后加载 .env 文件
     * 将 .env 中的变量设置到系统属性中，供 Spring Boot 配置文件使用
     */
    @PostConstruct
    public void loadEnv() {
        try {
            Dotenv dotenv = Dotenv.configure()
                    .directory("./")  // .env 文件所在目录（项目根目录）
                    .ignoreIfMissing()  // 如果 .env 文件不存在，不报错
                    .load();
            
            // 将 .env 中的变量设置到系统属性中
            dotenv.entries().forEach(entry -> {
                System.setProperty(entry.getKey(), entry.getValue());
            });
            
            System.out.println("✅ .env 文件加载成功");
        } catch (Exception e) {
            System.err.println("⚠️ 未找到 .env 文件，将使用默认配置");
        }
    }
}
