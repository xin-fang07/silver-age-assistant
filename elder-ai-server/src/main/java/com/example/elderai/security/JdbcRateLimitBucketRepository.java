package com.example.elderai.security;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/** MySQL 共享固定窗口计数器。 */
@Repository
public class JdbcRateLimitBucketRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcRateLimitBucketRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int increment(String bucketKey, long windowId, Instant expiresAt) {
        jdbcTemplate.update("""
                INSERT INTO rate_limit_bucket (bucket_key, window_id, request_count, expires_at)
                VALUES (?, ?, 1, FROM_UNIXTIME(?))
                ON DUPLICATE KEY UPDATE
                    request_count = request_count + 1,
                    expires_at = VALUES(expires_at)
                """, bucketKey, windowId, expiresAt.getEpochSecond());
        Integer count = jdbcTemplate.queryForObject("""
                SELECT request_count FROM rate_limit_bucket
                WHERE bucket_key = ? AND window_id = ?
                """, Integer.class, bucketKey, windowId);
        return count == null ? 1 : count;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int deleteExpired(int batchSize) {
        int safeBatchSize = Math.max(100, Math.min(10_000, batchSize));
        return jdbcTemplate.update("DELETE FROM rate_limit_bucket "
                + "WHERE expires_at < CURRENT_TIMESTAMP(3) LIMIT " + safeBatchSize);
    }
}
