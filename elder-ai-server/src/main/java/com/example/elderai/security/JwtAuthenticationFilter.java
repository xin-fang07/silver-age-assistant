package com.example.elderai.security;

import com.example.elderai.service.RedisCacheService;
import com.example.elderai.utils.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * 统一解析 Bearer JWT，并将用户身份写入 Spring Security 上下文。
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtUtils jwtUtils;
    private final RedisCacheService redisCacheService;

    public JwtAuthenticationFilter(JwtUtils jwtUtils, RedisCacheService redisCacheService) {
        this.jwtUtils = jwtUtils;
        this.redisCacheService = redisCacheService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");
        if (authorization != null
                && authorization.startsWith(BEARER_PREFIX)
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            String token = authorization.substring(BEARER_PREFIX.length()).trim();
            if (!token.isEmpty() && jwtUtils.validateToken(token)) {
                // Token 黑名单校验：退出登录后的 Token 立即失效
                String jti = jwtUtils.getId(token);
                if (jti != null && redisCacheService.hasKey("jwt:blacklist:" + jti)) {
                    filterChain.doFilter(request, response);
                    return;
                }
                String role = jwtUtils.getRole(token);
                AuthenticatedUser user = new AuthenticatedUser(
                        jwtUtils.getUserId(token),
                        jwtUtils.getUsername(token),
                        role
                );
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                user,
                                null,
                                List.of(new SimpleGrantedAuthority("ROLE_" + role))
                        );
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        filterChain.doFilter(request, response);
    }
}
