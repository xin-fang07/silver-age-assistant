package com.example.elderai.security;

import com.example.elderai.infrastructure.lock.DistributedTaskLockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;

/** 定期分批清理已过期的共享限流窗口。 */
@Component
public class RateLimitMaintenanceJob {

    private static final Logger log = LoggerFactory.getLogger(RateLimitMaintenanceJob.class);

    private final JdbcRateLimitBucketRepository repository;
    private final RateLimitService rateLimitService;
    private final DistributedTaskLockService taskLockService;
    private final int cleanupBatchSize;
    private final long cleanupIntervalMs;

    public RateLimitMaintenanceJob(
            JdbcRateLimitBucketRepository repository,
            RateLimitService rateLimitService,
            DistributedTaskLockService taskLockService,
            @Value("${app.security.rate-limit.cleanup.batch-size:5000}") int cleanupBatchSize,
            @Value("${app.security.rate-limit.cleanup.interval-ms:600000}") long cleanupIntervalMs) {
        this.repository = repository;
        this.rateLimitService = rateLimitService;
        this.taskLockService = taskLockService;
        this.cleanupBatchSize = cleanupBatchSize;
        this.cleanupIntervalMs = cleanupIntervalMs;
    }

    @Scheduled(
            fixedDelayString = "${app.security.rate-limit.cleanup.interval-ms:600000}",
            initialDelayString = "${app.security.rate-limit.cleanup.initial-delay-ms:60000}")
    public void cleanupExpiredBuckets() {
        if (!rateLimitService.usesJdbcStorage()) {
            return;
        }
        long atLeastMs = Math.max(1_000, cleanupIntervalMs - 5_000);
        taskLockService.runWithLock("rate-limit-cleanup",
                Duration.ofMinutes(15), Duration.ofMillis(atLeastMs), () -> {
                    int deleted = repository.deleteExpired(cleanupBatchSize);
                    if (deleted > 0) {
                        log.info("[共享限流维护] 已清理 {} 个过期窗口", deleted);
                    }
                });
    }
}
