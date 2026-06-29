package com.zhou;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot 应用启动类
 * 负责加载 .env 环境变量并启动应用
 */
@SpringBootApplication
public class Application {
    // http://localhost:8090/hello
    
    /**
     * 应用启动入口方法
     */
    public static void main(String[] args) {
        
        // 加载 .env 文件中的环境变量（忽略格式错误的条目）
        Dotenv dotenv = Dotenv.configure().ignoreIfMalformed().load();
        
        // 将 .env 中的环境变量设置为系统属性，供 Spring Boot 读取
        dotenv.entries().forEach(entry -> 
            System.setProperty(entry.getKey(), entry.getValue())
        );
        
        // 启动 Spring Boot 应用
        SpringApplication.run(Application.class, args);
    }
}