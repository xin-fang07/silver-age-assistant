package com.example.elderai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 应用自定义配置类
 * <p>
 * 使用 {@code @ConfigurationProperties(prefix = "app")} 将 application.yml 中
 * {@code app} 命名空间下的配置项自动绑定到 Java 对象。
 * </p>
 * <p>
 * 包含两个子配置：
 * <ul>
 *   <li><b>deepseek</b> —— DeepSeek AI 大模型 API 配置</li>
 *   <li><b>jwt</b> —— JWT 认证配置</li>
 * </ul>
 * </p>
 * <p>
 * 使用方式：在任意 Spring 管理的 Bean 中通过 @Autowired 注入 AppConfig，
 * 然后调用 getter 方法获取配置值。
 * 这是除 @Value 注解外的另一种配置读取方式，适合需要集中管理配置的场景。
 * </p>
 *
 * @author elder-ai-team
 */
@Configuration // 标记为 Spring 配置类
@ConfigurationProperties(prefix = "app") // 绑定 application.yml 中 app 前缀的配置
public class AppConfig {

    /** DeepSeek AI 相关配置 */
    private DeepSeekConfig deepseek;

    /** JWT 认证相关配置 */
    private JwtConfig jwt;

    // ==================== Getter & Setter ====================

    public DeepSeekConfig getDeepseek() {
        return deepseek;
    }

    public void setDeepseek(DeepSeekConfig deepseek) {
        this.deepseek = deepseek;
    }

    public JwtConfig getJwt() {
        return jwt;
    }

    public void setJwt(JwtConfig jwt) {
        this.jwt = jwt;
    }

    // ==================== 内部类：DeepSeek 配置 ====================

    /**
     * DeepSeek AI 大模型 API 配置
     * <p>
     * 对应 application.yml 中的 app.deepseek.api.* 配置项
     * </p>
     */
    public static class DeepSeekConfig {

        /** API 密钥 */
        private ApiConfig api;

        public ApiConfig getApi() {
            return api;
        }

        public void setApi(ApiConfig api) {
            this.api = api;
        }

        /**
         * DeepSeek API 详细配置（因 yml 中有中间层级 api）
         */
        public static class ApiConfig {
            /** DeepSeek API 密钥 */
            private String key;

            /** DeepSeek Chat Completions API 地址 */
            private String url;

            /** 使用的模型名称 */
            private String model;

            public String getKey() {
                return key;
            }

            public void setKey(String key) {
                this.key = key;
            }

            public String getUrl() {
                return url;
            }

            public void setUrl(String url) {
                this.url = url;
            }

            public String getModel() {
                return model;
            }

            public void setModel(String model) {
                this.model = model;
            }
        }
    }

    // ==================== 内部类：JWT 配置 ====================

    /**
     * JWT 认证配置
     * <p>
     * 对应 application.yml 中的 app.jwt.* 配置项
     * </p>
     */
    public static class JwtConfig {

        /** JWT 签名密钥（HMAC-SHA 算法） */
        private String secret;

        /** Token 过期时间（毫秒），默认 24 小时 */
        private long expiration;

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }

        public long getExpiration() {
            return expiration;
        }

        public void setExpiration(long expiration) {
            this.expiration = expiration;
        }
    }
}
