package com.example.elderai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 银发智能生活助手系统 - 主启动类
 * <p>
 * 基于 Spring Boot 3.x + MyBatis-Plus + MySQL 8.0 构建，
 * 集成 LLM（DeepSeek）为老年人提供智能生活辅助服务。
 * </p>
 *
 * @author elder-ai-team
 */
@SpringBootApplication // 标记为 Spring Boot 应用入口
@EnableScheduling      // 开启定时任务支持（用于每日健康提醒、天气推送等）
public class ElderAiApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(ElderAiApplication.class, args);
        String port = context.getEnvironment().getProperty("local.server.port",
                context.getEnvironment().getProperty("server.port", "8080"));
        System.out.println("============================================");
        System.out.println("  银发智能生活助手系统启动成功！");
        System.out.println("  访问地址: http://localhost:" + port);
        System.out.println("============================================");
    }
}
