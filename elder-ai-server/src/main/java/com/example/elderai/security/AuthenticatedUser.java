package com.example.elderai.security;

/**
 * 通过 JWT 认证后的当前用户身份。
 */
public record AuthenticatedUser(Long userId, String username, String role) {
}
