package com.example.elderai.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import jakarta.annotation.PostConstruct;

/**
 * JWT（JSON Web Token）工具类
 * <p>
 * 负责生成、解析和验证 JWT Token。用户在登录成功后，
 * 服务端生成一个包含用户信息的 Token 返回给前端；
 * 后续请求中前端携带该 Token，服务端解析验证后识别用户身份。
 * </p>
 * <p>
 * 使用 jjwt 0.12.x 新 API：
 * 签名密钥通过 {@code Keys.hmacShaKeyFor()} 生成，
 * 解析通过 {@code Jwts.parser().verifyWith(key).build()} 构建解析器。
 * </p>
 *
 * @author elder-ai-team
 */
@Component // 标记为 Spring 管理的 Bean，方便通过 @Autowired 注入
public class JwtUtils {

    /**
     * JWT 签名密钥（Base64 编码的字符串形式）
     * 从 application.yml 中的 app.jwt.secret 配置项读取
     */
    @Value("${app.jwt.secret}")
    private String secret;

    /**
     * Token 过期时间（毫秒）
     * 从 application.yml 中的 app.jwt.expiration 配置项读取，默认 24 小时
     */
    @Value("${app.jwt.expiration}")
    private long expiration;

    @PostConstruct
    void validateConfiguration() {
        if (secret == null || secret.length() < 32) {
            throw new IllegalStateException("JWT_SECRET 必须至少包含 32 个字符");
        }
        if (expiration <= 0) {
            throw new IllegalStateException("JWT_EXPIRATION 必须大于 0");
        }
    }

    // ==================== Token 生成 ====================

    /**
     * 根据用户信息生成 JWT Token
     * <p>
     * Token 中存储的 Claims（声明）包含 userId、username 和 role，
     * 后续解析时可以直接从中获取，无需查询数据库。
     * </p>
     *
     * @param userId   用户 ID
     * @param username 用户名
     * @param role     用户角色（如 "USER"、"ADMIN"）
     * @return 生成的 JWT Token 字符串
     */
    public String generateToken(Long userId, String username, String role) {
        // 1. 计算 Token 的过期时间：当前时间 + 配置的过期时长
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + expiration);

        // 2. 构建自定义 Claims（负载数据），存放用户身份信息
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("role", role);
        claims.put("jti", UUID.randomUUID().toString());

        // 3. 生成 HMAC-SHA 签名密钥
        //    使用配置字符串的 UTF-8 字节生成密钥
        SecretKey key = getSigningKey();

        // 4. 构建并签发 JWT Token（jjwt 0.12.x 新 API）
        String token = Jwts.builder()
                .claims(claims)                          // 设置自定义 Claims
                .subject(username)                       // 设置主题（一般为用户名）
                .issuedAt(now)                           // 签发时间
                .expiration(expirationDate)              // 过期时间
                .signWith(key)                           // 使用 HMAC-SHA 密钥签名
                .compact();                              // 压缩为字符串

        return token;
    }

    // ==================== Token 解析 ====================

    /**
     * 解析 JWT Token，获取其中所有的 Claims（声明）
     * <p>
     * 如果 Token 无效（篡改、过期、格式错误等），会抛出异常，
     * 由调用方捕获处理。
     * </p>
     *
     * @param token JWT Token 字符串
     * @return Token 中包含的 Claims 对象
     */
    public Claims parseToken(String token) {
        // jjwt 0.12.x 新 API：先构建 JwtParser，再解析
        return Jwts.parser()
                .verifyWith(getSigningKey())  // 设置签名验证密钥
                .build()                       // 构建解析器
                .parseSignedClaims(token)      // 解析并验证签名
                .getPayload();                 // 获取 Claims 负载
    }

    /**
     * 从 Token 中获取用户 ID
     *
     * @param token JWT Token 字符串
     * @return 用户 ID，如果不存在则返回 null
     */
    public Long getUserId(String token) {
        Claims claims = parseToken(token);
        Object userIdObj = claims.get("userId");
        if (userIdObj instanceof Integer) {
            return ((Integer) userIdObj).longValue();
        } else if (userIdObj instanceof Long) {
            return (Long) userIdObj;
        }
        return null;
    }

    /**
     * 从 Token 中获取用户名
     *
     * @param token JWT Token 字符串
     * @return 用户名
     */
    public String getUsername(String token) {
        Claims claims = parseToken(token);
        return claims.get("username", String.class);
    }

    /**
     * 从 Token 中获取用户角色
     *
     * @param token JWT Token 字符串
     * @return 用户角色（如 "USER"、"ADMIN"）
     */
    public String getRole(String token) {
        Claims claims = parseToken(token);
        return claims.get("role", String.class);
    }

    /**
     * 获取 Token 唯一标识（jti）
     */
    public String getId(String token) {
        return parseToken(token).get("jti", String.class);
    }

    /**
     * 获取 Token 过期时间
     */
    public Date getExpiration(String token) {
        return parseToken(token).getExpiration();
    }

    /**
     * 验证 Token 是否有效
     * <p>
     * 通过尝试解析 Token 来判断：能成功解析且不抛异常即为有效。
     * 解析失败（过期、签名不匹配、格式错误等）返回 false。
     * </p>
     *
     * @param token JWT Token 字符串
     * @return true 表示有效，false 表示无效
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            // 解析过程中出现任何异常都视为 Token 无效
            return false;
        }
    }

    // ==================== 内部辅助方法 ====================

    /**
     * 获取 HMAC-SHA 签名密钥
     * <p>
     * 将配置的 secret 字符串的字节直接作为 HMAC-SHA 密钥，使用 Keys.hmacShaKeyFor()
     * 生成符合 jjwt 要求的 SecretKey 对象。secret 须至少 256 位（32 字符）。
     * </p>
     *
     * @return SecretKey 签名密钥对象
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }
}
