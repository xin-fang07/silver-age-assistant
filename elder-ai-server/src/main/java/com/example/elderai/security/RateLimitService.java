package com.example.elderai.security;

import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 固定窗口限流器。默认通过 MySQL 在多个 Java 实例间共享，存储异常时降级为进程内计数。
 */
@Component
public class RateLimitService {

    private static final Logger log = LoggerFactory.getLogger(RateLimitService.class);
    private static final int CLEANUP_THRESHOLD = 10_000;
    private static final long WARNING_INTERVAL_MS = 60_000;

    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final Clock clock;
    private final JdbcRateLimitBucketRepository jdbcRepository;
    private final MeterRegistry meterRegistry;
    private final String storage;
    private final AtomicLong lastStorageWarning = new AtomicLong();

    @Autowired
    public RateLimitService(
            JdbcRateLimitBucketRepository jdbcRepository,
            MeterRegistry meterRegistry,
            @Value("${app.security.rate-limit.storage:jdbc}") String storage) {
        this(Clock.systemUTC(), jdbcRepository, storage, meterRegistry);
    }

    RateLimitService(Clock clock) {
        this(clock, null, "memory", null);
    }

    RateLimitService(Clock clock, JdbcRateLimitBucketRepository jdbcRepository,
                     String storage, MeterRegistry meterRegistry) {
        this.clock = clock;
        this.jdbcRepository = jdbcRepository;
        this.meterRegistry = meterRegistry;
        this.storage = normalizeStorage(storage);
    }

    public Decision tryAcquire(String key, int maxRequests, int windowSeconds) {
        if (maxRequests <= 0 || windowSeconds <= 0) {
            return new Decision(true, Integer.MAX_VALUE, 0);
        }

        long now = clock.instant().getEpochSecond();
        long windowId = now / windowSeconds;
        int count;
        String backend = storage;

        if ("jdbc".equals(storage)) {
            try {
                Instant expiresAt = Instant.ofEpochSecond((windowId + 1) * windowSeconds);
                count = jdbcRepository.increment(hashKey(key), windowId, expiresAt);
            } catch (DataAccessException ex) {
                warnStorageFallback(ex);
                backend = "memory_fallback";
                count = incrementMemory(key, windowId);
            }
        } else {
            count = incrementMemory(key, windowId);
        }

        int remaining = Math.max(0, maxRequests - count);
        long retryAfter = Math.max(1, ((windowId + 1) * windowSeconds) - now);
        boolean allowed = count <= maxRequests;
        record(backend, allowed ? "allowed" : "rejected");
        return new Decision(allowed, remaining, retryAfter);
    }

    public boolean usesJdbcStorage() {
        return "jdbc".equals(storage);
    }

    private int incrementMemory(String key, long windowId) {
        Bucket bucket = buckets.compute(key, (ignored, current) -> {
            if (current == null || current.windowId() != windowId) {
                return new Bucket(windowId, 1);
            }
            return new Bucket(windowId, current.count() + 1);
        });

        if (buckets.size() > CLEANUP_THRESHOLD) {
            buckets.entrySet().removeIf(entry -> entry.getValue().windowId() < windowId - 1);
        }
        return bucket.count();
    }

    private String hashKey(String key) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(key.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is unavailable", ex);
        }
    }

    private String normalizeStorage(String value) {
        String normalized = value == null ? "jdbc" : value.trim().toLowerCase(Locale.ROOT);
        if (!"jdbc".equals(normalized) && !"memory".equals(normalized)) {
            throw new IllegalArgumentException("RATE_LIMIT_STORAGE must be jdbc or memory");
        }
        if ("jdbc".equals(normalized) && jdbcRepository == null) {
            throw new IllegalArgumentException("JDBC rate limit storage requires a repository");
        }
        return normalized;
    }

    private void record(String backend, String outcome) {
        if (meterRegistry != null) {
            meterRegistry.counter("elder.rate.limit.requests",
                    "backend", backend, "outcome", outcome).increment();
        }
    }

    private void warnStorageFallback(DataAccessException ex) {
        long now = System.currentTimeMillis();
        long previous = lastStorageWarning.get();
        if (now - previous >= WARNING_INTERVAL_MS && lastStorageWarning.compareAndSet(previous, now)) {
            log.error("[共享限流降级] MySQL 计数器不可用，暂用进程内限流，原因={}",
                    ex.getClass().getSimpleName());
        }
    }

    record Bucket(long windowId, int count) {
    }

    public record Decision(boolean allowed, int remaining, long retryAfterSeconds) {
    }
}
