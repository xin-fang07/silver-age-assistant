package com.example.elderai.security;

import com.example.elderai.common.BusinessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 读取统一认证上下文，避免业务 Controller 重复解析 JWT。
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static AuthenticatedUser currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser user)) {
            throw new BusinessException(401, "请先登录");
        }
        return user;
    }

    public static Long currentUserId() {
        return currentUser().userId();
    }
}
