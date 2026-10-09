package com.example.elderai.config;

import com.example.elderai.common.Result;
import com.example.elderai.security.ClientIpResolver;
import com.example.elderai.security.JwtAuthenticationFilter;
import com.example.elderai.service.RedisCacheService;
import com.example.elderai.security.RateLimitFilter;
import com.example.elderai.security.RateLimitService;
import com.example.elderai.utils.JwtUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;

/**
 * Spring Security 配置类
 * <p>
 * 本系统仅使用 Spring Security 的 BCrypt 密码加密功能进行用户密码的哈希处理，
 * 认证鉴权通过 JWT 自行实现。因此需要：
 * <ul>
 *   <li>禁用 Spring Security 默认的表单登录页和 HTTP Basic 认证</li>
 *   <li>禁用 CSRF 保护（RESTful API 通常不需要 CSRF）</li>
 *   <li>允许所有请求匿名访问（JWT 过滤器自行处理认证逻辑）</li>
 *   <li>暴露 BCryptPasswordEncoder Bean 供业务代码注入使用</li>
 * </ul>
 * </p>
 *
 * @author elder-ai-team
 */
@Configuration        // 标记为 Spring 配置类
@EnableWebSecurity    // 启用 Spring Security 的 Web 安全功能
public class SecurityConfig {

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtUtils jwtUtils, RedisCacheService redisCacheService) {
        return new JwtAuthenticationFilter(jwtUtils, redisCacheService);
    }

    /**
     * Security 过滤器链配置
     * <p>
     * 禁用所有默认安全策略，放行所有请求。
     * 实际的认证鉴权由自定义 JWT 过滤器处理。
     * </p>
     *
     * @param http HttpSecurity 安全配置构建器
     * @return SecurityFilterChain 过滤器链
     * @throws Exception 配置异常
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   JwtAuthenticationFilter jwtAuthenticationFilter,
                                                   RateLimitService rateLimitService,
                                                   ClientIpResolver clientIpResolver,
                                                   ObjectMapper objectMapper,
                                                   @Value("${app.security.rate-limit.enabled:true}") boolean rateLimitEnabled,
                                                   @Value("${app.security.rate-limit.login.max-requests:10}") int loginMax,
                                                   @Value("${app.security.rate-limit.login.window-seconds:60}") int loginWindow,
                                                   @Value("${app.security.rate-limit.register.max-requests:5}") int registerMax,
                                                   @Value("${app.security.rate-limit.register.window-seconds:3600}") int registerWindow,
                                                   @Value("${app.security.rate-limit.ai.max-requests:20}") int aiMax,
                                                   @Value("${app.security.rate-limit.ai.window-seconds:60}") int aiWindow,
                                                   @Value("${app.security.rate-limit.upload.max-requests:20}") int uploadMax,
                                                   @Value("${app.security.rate-limit.upload.window-seconds:60}") int uploadWindow) throws Exception {
        RateLimitFilter rateLimitFilter = new RateLimitFilter(
                rateLimitService,
                clientIpResolver,
                objectMapper,
                rateLimitEnabled,
                new RateLimitFilter.Policy("login", loginMax, loginWindow, false),
                new RateLimitFilter.Policy("register", registerMax, registerWindow, false),
                new RateLimitFilter.Policy("ai", aiMax, aiWindow, true),
                new RateLimitFilter.Policy("upload", uploadMax, uploadWindow, true)
        );

        http
            // 禁用 CSRF 保护：前后端分离 + JWT 认证，不需要 CSRF Token
            .csrf(AbstractHttpConfigurer::disable)

            .cors(cors -> {})

            // 禁用默认的登录表单
            .formLogin(AbstractHttpConfigurer::disable)

            // 禁用 HTTP Basic 认证
            .httpBasic(AbstractHttpConfigurer::disable)

            // 禁用 Logout 端点
            .logout(AbstractHttpConfigurer::disable)

            // 不需要 Session（JWT 无状态认证）
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/api/auth/login", "/api/auth/register", "/api/auth/logout", "/error").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/contact/message").permitAll()
                .requestMatchers("/actuator/health", "/actuator/health/liveness",
                        "/actuator/health/readiness").permitAll()
                .requestMatchers("/actuator/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/news/**", "/uploads/**").permitAll()
                .requestMatchers("/ws/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/device/upload",
                        "/api/device/push-reminder", "/api/device/confirm-reminder").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/device/emergency").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/admin/warning-center/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint((request, response, exception) ->
                        writeSecurityError(response, objectMapper, 401, "请先登录或登录状态已失效"))
                .accessDeniedHandler((request, response, exception) ->
                        writeSecurityError(response, objectMapper, 403, "没有访问权限"))
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterAfter(rateLimitFilter, JwtAuthenticationFilter.class);

        return http.build();
    }

    private static void writeSecurityError(HttpServletResponse response,
                                           ObjectMapper objectMapper,
                                           int status,
                                           String message) throws IOException {
        response.setStatus(status);
        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), Result.error(status, message));
    }

    /**
     * BCrypt 密码编码器
     * <p>
     * 用于用户注册时对明文密码进行哈希处理，以及登录时比对密码。
     * BCrypt 是一种基于 Blowfish 的慢哈希算法，能有效抵抗彩虹表攻击。
     * </p>
     *
     * @return BCryptPasswordEncoder 实例
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
