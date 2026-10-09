package com.example.elderai.security;

import com.example.elderai.common.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 登录、注册、AI 调用和上传接口的频率保护。
 */
public class RateLimitFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitFilter.class);

    private final RateLimitService rateLimitService;
    private final ClientIpResolver clientIpResolver;
    private final ObjectMapper objectMapper;
    private final boolean enabled;
    private final Policy loginPolicy;
    private final Policy registerPolicy;
    private final Policy aiPolicy;
    private final Policy uploadPolicy;

    public RateLimitFilter(RateLimitService rateLimitService,
                           ClientIpResolver clientIpResolver,
                           ObjectMapper objectMapper,
                           boolean enabled,
                           Policy loginPolicy,
                           Policy registerPolicy,
                           Policy aiPolicy,
                           Policy uploadPolicy) {
        this.rateLimitService = rateLimitService;
        this.clientIpResolver = clientIpResolver;
        this.objectMapper = objectMapper;
        this.enabled = enabled;
        this.loginPolicy = loginPolicy;
        this.registerPolicy = registerPolicy;
        this.aiPolicy = aiPolicy;
        this.uploadPolicy = uploadPolicy;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        Policy policy = enabled ? resolvePolicy(request) : null;
        if (policy == null) {
            filterChain.doFilter(request, response);
            return;
        }

        String identity = policy.userScoped() ? currentUserIdentity(request) : clientIpResolver.resolve(request);
        String key = policy.name() + ':' + identity;
        RateLimitService.Decision decision =
                rateLimitService.tryAcquire(key, policy.maxRequests(), policy.windowSeconds());

        response.setHeader("X-RateLimit-Limit", String.valueOf(policy.maxRequests()));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(decision.remaining()));
        if (decision.allowed()) {
            filterChain.doFilter(request, response);
            return;
        }

        log.warn("[接口限流] policy={} identity={} method={} uri={}",
                policy.name(), identity, request.getMethod(), request.getRequestURI());
        response.setStatus(429);
        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setHeader("Retry-After", String.valueOf(decision.retryAfterSeconds()));
        response.setHeader("Cache-Control", "no-store");
        objectMapper.writeValue(response.getWriter(),
                Result.error(429, "操作过于频繁，请稍后再试"));
    }

    private Policy resolvePolicy(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();
        if ("POST".equals(method) && "/api/auth/login".equals(path)) {
            return loginPolicy;
        }
        if ("POST".equals(method) && "/api/auth/register".equals(path)) {
            return registerPolicy;
        }
        if (("POST".equals(method) && "/api/chat/ask".equals(path))
                || ("GET".equals(method) && "/api/health/advice".equals(path))) {
            return aiPolicy;
        }
        if ("POST".equals(method)
                && ("/api/common/upload".equals(path) || "/api/common/upload-avatar".equals(path))) {
            return uploadPolicy;
        }
        return null;
    }

    private String currentUserIdentity(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AuthenticatedUser user) {
            return "user-" + user.userId();
        }
        return "ip-" + clientIpResolver.resolve(request);
    }

    public record Policy(String name, int maxRequests, int windowSeconds, boolean userScoped) {
    }
}
