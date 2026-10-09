package com.example.elderai.security;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RateLimitServiceTest {

    @Test
    void shouldAllowUntilLimitAndThenReject() {
        RateLimitService service = new RateLimitService(
                Clock.fixed(Instant.ofEpochSecond(100), ZoneOffset.UTC));

        assertTrue(service.tryAcquire("login:127.0.0.1", 2, 60).allowed());
        assertTrue(service.tryAcquire("login:127.0.0.1", 2, 60).allowed());
        RateLimitService.Decision rejected = service.tryAcquire("login:127.0.0.1", 2, 60);

        assertFalse(rejected.allowed());
        assertEquals(0, rejected.remaining());
        assertTrue(rejected.retryAfterSeconds() > 0);
    }

    @Test
    void shouldIsolateDifferentIdentities() {
        RateLimitService service = new RateLimitService(
                Clock.fixed(Instant.ofEpochSecond(100), ZoneOffset.UTC));

        assertTrue(service.tryAcquire("ai:user-1", 1, 60).allowed());
        assertFalse(service.tryAcquire("ai:user-1", 1, 60).allowed());
        assertTrue(service.tryAcquire("ai:user-2", 1, 60).allowed());
    }

    @Test
    void shouldResetAfterWindowChanges() {
        MutableClock clock = new MutableClock(Instant.ofEpochSecond(100));
        RateLimitService service = new RateLimitService(clock);

        assertTrue(service.tryAcquire("login:ip", 1, 60).allowed());
        assertFalse(service.tryAcquire("login:ip", 1, 60).allowed());
        clock.setInstant(Instant.ofEpochSecond(121));
        assertTrue(service.tryAcquire("login:ip", 1, 60).allowed());
    }

    @Test
    void shouldUseSharedJdbcCounter() {
        Clock clock = Clock.fixed(Instant.ofEpochSecond(100), ZoneOffset.UTC);
        JdbcRateLimitBucketRepository repository = mock(JdbcRateLimitBucketRepository.class);
        when(repository.increment(anyString(), eq(1L), any()))
                .thenReturn(1, 2, 3);
        RateLimitService service = new RateLimitService(
                clock, repository, "jdbc", new SimpleMeterRegistry());

        assertTrue(service.tryAcquire("login:127.0.0.1", 2, 60).allowed());
        assertTrue(service.tryAcquire("login:127.0.0.1", 2, 60).allowed());
        assertFalse(service.tryAcquire("login:127.0.0.1", 2, 60).allowed());

        verify(repository, org.mockito.Mockito.times(3))
                .increment(anyString(), eq(1L), any());
    }

    @Test
    void shouldFallbackToMemoryWhenSharedCounterFails() {
        Clock clock = Clock.fixed(Instant.ofEpochSecond(100), ZoneOffset.UTC);
        JdbcRateLimitBucketRepository repository = mock(JdbcRateLimitBucketRepository.class);
        when(repository.increment(anyString(), anyLong(), any()))
                .thenThrow(new DataAccessResourceFailureException("offline"));
        RateLimitService service = new RateLimitService(
                clock, repository, "jdbc", new SimpleMeterRegistry());

        assertTrue(service.tryAcquire("login:ip", 1, 60).allowed());
        assertFalse(service.tryAcquire("login:ip", 1, 60).allowed());
    }

    private static final class MutableClock extends Clock {
        private Instant instant;

        private MutableClock(Instant instant) {
            this.instant = instant;
        }

        void setInstant(Instant instant) {
            this.instant = instant;
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return instant;
        }
    }
}
