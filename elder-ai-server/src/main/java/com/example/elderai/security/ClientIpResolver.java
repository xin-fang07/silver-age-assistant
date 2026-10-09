package com.example.elderai.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * 统一解析客户端 IP。默认不信任可被客户端伪造的代理请求头。
 */
@Component
public class ClientIpResolver {

    private static final Pattern SAFE_IP = Pattern.compile("[0-9a-fA-F:.]{2,45}");
    private final boolean trustProxyHeaders;

    public ClientIpResolver(@Value("${app.security.trust-proxy-headers:false}") boolean trustProxyHeaders) {
        this.trustProxyHeaders = trustProxyHeaders;
    }

    public String resolve(HttpServletRequest request) {
        String remoteAddress = normalize(request.getRemoteAddr());
        if (!trustProxyHeaders) {
            return remoteAddress;
        }

        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            String firstHop = normalize(forwardedFor.split(",", 2)[0].trim());
            if (!"unknown".equals(firstHop)) {
                return firstHop;
            }
        }

        String realIp = normalize(request.getHeader("X-Real-IP"));
        return "unknown".equals(realIp) ? remoteAddress : realIp;
    }

    private String normalize(String ip) {
        if (ip == null || !SAFE_IP.matcher(ip.trim()).matches()) {
            return "unknown";
        }
        return ip.trim();
    }
}
