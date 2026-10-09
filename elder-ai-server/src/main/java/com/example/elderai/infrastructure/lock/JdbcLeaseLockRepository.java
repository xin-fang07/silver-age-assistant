package com.example.elderai.infrastructure.lock;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** 使用 MySQL 时间维护跨实例任务租约。 */
@Repository
public class JdbcLeaseLockRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcLeaseLockRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean tryAcquire(String lockName, String ownerId,
                              long lockAtMostSeconds, long lockAtLeastSeconds) {
        jdbcTemplate.update("""
                INSERT IGNORE INTO scheduler_lock
                    (lock_name, locked_until, lock_at_least_until, locked_at, locked_by)
                VALUES (?, DATE_SUB(UTC_TIMESTAMP(3), INTERVAL 1 SECOND),
                        DATE_SUB(UTC_TIMESTAMP(3), INTERVAL 1 SECOND), UTC_TIMESTAMP(3), ?)
                """, lockName, ownerId);

        int updated = jdbcTemplate.update("""
                UPDATE scheduler_lock
                SET locked_until = TIMESTAMPADD(SECOND, ?, UTC_TIMESTAMP(3)),
                    lock_at_least_until = TIMESTAMPADD(SECOND, ?, UTC_TIMESTAMP(3)),
                    locked_at = UTC_TIMESTAMP(3),
                    locked_by = ?
                WHERE lock_name = ?
                  AND locked_until <= UTC_TIMESTAMP(3)
                """, lockAtMostSeconds, lockAtLeastSeconds, ownerId, lockName);
        return updated == 1;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void release(String lockName, String ownerId) {
        jdbcTemplate.update("""
                UPDATE scheduler_lock
                SET locked_until = GREATEST(UTC_TIMESTAMP(3), lock_at_least_until)
                WHERE lock_name = ? AND locked_by = ?
                """, lockName, ownerId);
    }
}
