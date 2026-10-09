package com.example.elderai.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * SpringDoc OpenAPI 配置类
 * <p>
 * 配置 Swagger 接口文档的基本信息、JWT 认证方案等。
 * 启动后访问：
 * - Swagger UI: http://localhost:8080/swagger-ui.html
 * - OpenAPI JSON: http://localhost:8080/v3/api-docs
 * </p>
 *
 * @author elder-ai-team
 */
@Configuration
public class OpenApiConfig {

    /** JWT 认证方案名称 */
    private static final String SECURITY_SCHEME_NAME = "BearerAuth";

    /**
     * 配置 OpenAPI 文档元信息
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                // 文档基本信息
                .info(new Info()
                        .title("银发智能生活助手系统 API")
                        .description("基于 LLM 的银发智能生活助手系统后端接口文档。" +
                                "包含智能问答、生活提醒、紧急求助、健康管理、家属绑定、管理后台等模块。")
                        .version("1.1.0")
                        .contact(new Contact()
                                .name("elder-ai-team")
                                .email("support@elder-ai.example.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                // 服务器地址
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("本地开发环境")
                ))
                // 全局安全认证（JWT Bearer Token）
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .name(SECURITY_SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("请输入 JWT Token，格式：Bearer {token}")));
    }
}
