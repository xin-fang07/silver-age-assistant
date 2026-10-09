package com.example.elderai.config;

import com.example.elderai.service.RedisCacheService;
import com.example.elderai.utils.JwtUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

/** 在 WebSocket 升级前验证 JWT，并把可信用户身份写入会话。 */
@Component
public class JwtWebSocketHandshakeInterceptor implements HandshakeInterceptor {
    private final JwtUtils jwtUtils;
    private final RedisCacheService redisCacheService;

    public JwtWebSocketHandshakeInterceptor(JwtUtils jwtUtils, RedisCacheService redisCacheService) {
        this.jwtUtils = jwtUtils;
        this.redisCacheService = redisCacheService;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        String token = UriComponentsBuilder.fromUri(request.getURI()).build()
                .getQueryParams().getFirst("token");
        if (token == null || !jwtUtils.validateToken(token)) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }
        String jti = jwtUtils.getId(token);
        if (jti != null && redisCacheService.hasKey("jwt:blacklist:" + jti)) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }
        Long userId = jwtUtils.getUserId(token);
        if (userId == null) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }
        attributes.put("userId", userId);
        attributes.put("role", jwtUtils.getRole(token));
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) { }
}
