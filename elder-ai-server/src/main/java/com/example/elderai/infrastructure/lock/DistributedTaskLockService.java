package com.example.elderai.infrastructure.lock;

import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;

/**
 * 数据库租约任务锁。数据库暂时不可用时退回进程内互斥，保证单实例功能仍可运行。
 */
@Component
public class DistributedTaskLockService {

    private static final Logger log = LoggerFactory.getLogger(DistributedTaskLockService.class);
    private static final Pattern LOCK_NAME = Pattern.compile("[a-z0-9._-]{1,100}");
    private static final long WARNING_INTERVAL_MS = 60_000;

    private final JdbcLeaseLockRepository repository;
    private final MeterRegistry meterRegistry;
    private final boolean enabled;
    private final String ownerId;
    private final ConcurrentHashMap<String, AtomicBoolean> localFallbackLocks = new ConcurrentHashMap<>();
    private final AtomicLong lastStorageWarning = new AtomicLong();

    public DistributedTaskLockService(
            JdbcLeaseLockRepository repository,
            MeterRegistry meterRegistry,
            @Value("${app.scheduler.lock.enabled:true}") boolean enabled,
            @Value("${app.instance-id:}") String configuredInstanceId) {
        this.repository = repository;
        this.meterRegistry = meterRegistry;
        this.enabled = enabled;
        this.ownerId = normalizeOwnerId(configuredInstanceId);
    }

    public boolean runWithLock(String lockName, Duration lockAtMost,
                               Duration lockAtLeast, Runnable task) {
        validate(lockName, lockAtMost, lockAtLeast, task);
        if (!enabled) {
            return runTask(lockName, "disabled", task, null);
        }

        boolean acquired;
        boolean localFallback = false;
        try {
            acquired = repository.tryAcquire(lockName, ownerId,
                    lockAtMost.toSeconds(), lockAtLeast.toSeconds());
        } catch (DataAccessException ex) {
            warnStorageFallback(ex);
            localFallback = true;
            acquired = localFallbackLocks.computeIfAbsent(lockName, ignored -> new AtomicBoolean())
                    .compareAndSet(false, true);
        }

        String backend = localFallback ? "memory_fallback" : "jdbc";
        if (!acquired) {
            record(lockName, backend, "skipped");
            log.debug("[定时任务锁] task={} 未获得租约，当前实例跳过", lockName);
            return false;
        }

        return runTask(lockName, backend, task, localFallback);
    }

    public String instanceId() {
        return ownerId;
    }

    private boolean runTask(String lockName, String backend, Runnable task, Boolean localFallback) {
        try {
            task.run();
            record(lockName, backend, "success");
            return true;
        } catch (RuntimeException | Error ex) {
            record(lockName, backend, "failure");
            throw ex;
        } finally {
            if (localFallback != null) {
                if (localFallback) {
                    localFallbackLocks.get(lockName).set(false);
                } else {
                    try {
                        repository.release(lockName, ownerId);
                    } catch (DataAccessException ex) {
                        warnStorageFallback(ex);
                    }
                }
            }
        }
    }

    private void record(String lockName, String backend, String outcome) {
        meterRegistry.counter("elder.scheduler.executions",
                "task", lockName, "backend", backend, "outcome", outcome).increment();
    }

    private void warnStorageFallback(DataAccessException ex) {
        long now = System.currentTimeMillis();
        long previous = lastStorageWarning.get();
        if (now - previous >= WARNING_INTERVAL_MS && lastStorageWarning.compareAndSet(previous, now)) {
            log.error("[定时任务锁降级] MySQL 租约不可用，暂用进程内互斥，原因={}",
                    ex.getClass().getSimpleName());
        }
    }

    private void validate(String lockName, Duration lockAtMost,
                          Duration lockAtLeast, Runnable task) {
        if (lockName == null || !LOCK_NAME.matcher(lockName).matches()) {
            throw new IllegalArgumentException("Invalid scheduler lock name");
        }
        if (task == null || lockAtMost == null || lockAtLeast == null
                || lockAtLeast.isNegative() || lockAtMost.isNegative()
                || lockAtMost.compareTo(Duration.ofSeconds(1)) < 0
                || lockAtLeast.compareTo(lockAtMost) > 0
                || lockAtMost.toSeconds() > 86_400) {
            throw new IllegalArgumentException("Invalid scheduler lock duration");
        }
    }

    private String normalizeOwnerId(String configured) {
        String value = configured == null ? "" : configured.trim();
        if (value.isEmpty()) {
            String host = System.getenv("HOSTNAME");
            if (host == null || host.isBlank()) {
                host = System.getenv("COMPUTERNAME");
            }
            if (host == null || host.isBlank()) {
                host = "local";
            }
            value = host + '-' + ProcessHandle.current().pid() + '-'
                    + UUID.randomUUID().toString().substring(0, 8);
        }
        value = value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9._-]", "-");
        return value.substring(0, Math.min(value.length(), 100));
    }
}
