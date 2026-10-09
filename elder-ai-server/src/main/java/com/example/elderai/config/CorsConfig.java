package com.example.elderai.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Arrays;

/**
 * CORS 跨域配置
 * <p>
 * 前后端分离开发时，前端（如运行在 localhost:5173）和后端（localhost:8080）
 * 属于不同源，浏览器会拦截跨域请求。此配置允许所有来源的跨域访问。
 * </p>
 * <p>
 * 注意：生产环境应限制 allowedOrigins 为具体的前端域名，而非使用通配符 *。
 * </p>
 *
 * @author elder-ai-team
 */
@Configuration // 标记为 Spring 配置类
public class CorsConfig {

    /**
     * 注册 CORS 过滤器
     * <p>
     * 使用 CorsFilter 方式配置跨域，优先级高于 WebMvcConfigurer 配置方式，
     * 能确保在所有请求到达 Controller 前都已添加跨域响应头。
     * </p>
     *
     * @return CorsFilter 实例
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${app.cors.allowed-origins}") String allowedOrigins) {
        // 1. 创建 CORS 配置对象
        CorsConfiguration config = new CorsConfiguration();

        java.util.List<String> origins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .toList();
        if (origins.isEmpty() || origins.contains("*")) {
            throw new IllegalStateException("CORS_ALLOWED_ORIGINS 必须配置明确的前端来源，不能使用通配符 *");
        }
        config.setAllowedOrigins(origins);

        // 允许携带 Cookie / Token 等凭证信息
        config.setAllowCredentials(true);

        // 允许所有请求头
        config.setAllowedHeaders(java.util.List.of("Authorization", "Content-Type", "Accept"));

        // 允许所有 HTTP 方法（GET, POST, PUT, DELETE, OPTIONS 等）
        config.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setMaxAge(3600L);

        // 2. 将配置注册到路径映射（/** 表示对所有路径生效）
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        // 3. 返回 CORS 过滤器
        return source;
    }
}
