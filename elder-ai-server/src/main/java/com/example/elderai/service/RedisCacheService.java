package com.example.elderai.service;

import com.alibaba.fastjson2.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Set;

/**
 * Redis 缓存统一封装。
 * <p>
 * 所有缓存读写均做降级处理：当 Redis 未启用或不可用（连接失败）时，
 * 方法安静返回（get 返回 null / set 忽略），不影响主流程。
 * 序列化统一使用 Fastjson2 的 JSON 字符串（与项目已有依赖一致）。
 * </p>
 */
@Service
public class RedisCacheService {

    private static final Logger log = LoggerFactory.getLogger(RedisCacheService.class);

    private final StringRedisTemplate stringRedisTemplate;

    @Value("${elder.redis.enabled:true}")
    private boolean enabled;

    public RedisCacheService(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /** 读取字符串值 */
    public String get(String key) {
        if (!enabled) return null;
        try {
            return stringRedisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.warn("Redis 读取失败 key={}: {}", key, e.getMessage());
            return null;
        }
    }

    /** 读取并反序列化为对象 */
    public <T> T getObject(String key, Class<T> clazz) {
        String value = get(key);
        if (value == null) return null;
        try {
            return JSON.parseObject(value, clazz);
        } catch (Exception e) {
            log.warn("Redis 反序列化失败 key={}: {}", key, e.getMessage());
            return null;
        }
    }

    /** 写入对象，TTL 以分钟为单位 */
    public void setObject(String key, Object value, long ttlMinutes) {
        if (!enabled) return;
        try {
            stringRedisTemplate.opsForValue().set(key, JSON.toJSONString(value), Duration.ofMinutes(ttlMinutes));
        } catch (Exception e) {
            log.warn("Redis 写入失败 key={}: {}", key, e.getMessage());
        }
    }

    /** 写入对象，TTL 以秒为单位（用于 Token 黑名单，TTL=Token 剩余有效期） */
    public void setObjectSeconds(String key, Object value, long ttlSeconds) {
        if (!enabled) return;
        try {
            stringRedisTemplate.opsForValue().set(key, JSON.toJSONString(value), Duration.ofSeconds(ttlSeconds));
        } catch (Exception e) {
            log.warn("Redis 写入失败 key={}: {}", key, e.getMessage());
        }
    }

    public boolean hasKey(String key) {
        if (!enabled) return false;
        try {
            return Boolean.TRUE.equals(stringRedisTemplate.hasKey(key));
        } catch (Exception e) {
            log.warn("Redis 判断 key 失败 key={}: {}", key, e.getMessage());
            return false;
        }
    }

    public void delete(String key) {
        if (!enabled) return;
        try {
            stringRedisTemplate.delete(key);
        } catch (Exception e) {
            log.warn("Redis 删除失败 key={}: {}", key, e.getMessage());
        }
    }

    /** 按前缀批量删除（用于列表类缓存失效，如 news:list:*） */
    public void deleteByPrefix(String prefix) {
        if (!enabled) return;
        try {
            // 使用 SCAN 迭代删除，避免 KEYS 在生产环境阻塞 Redis
            ScanOptions options = ScanOptions.scanOptions().match(prefix + "*").count(100).build();
            try (var cursor = stringRedisTemplate.scan(options)) {
                while (cursor.hasNext()) {
                    stringRedisTemplate.delete(cursor.next());
                }
            }
        } catch (Exception e) {
            log.warn("Redis 批量删除失败 prefix={}: {}", prefix, e.getMessage());
        }
    }
}
