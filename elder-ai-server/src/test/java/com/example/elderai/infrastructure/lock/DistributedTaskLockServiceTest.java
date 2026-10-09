package com.example.elderai.infrastructure.lock;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class DistributedTaskLockServiceTest {

    @Test
    void shouldSkipTaskWhenAnotherInstanceOwnsLease() {
        JdbcLeaseLockRepository repository = mock(JdbcLeaseLockRepository.class);
        when(repository.tryAcquire(anyString(), anyString(), anyLong(), anyLong())).thenReturn(false);
        DistributedTaskLockService service = service(repository);
        AtomicInteger executions = new AtomicInteger();

        boolean executed = service.runWithLock("reminder-expiration",
                Duration.ofMinutes(5), Duration.ofSeconds(55), executions::incrementAndGet);

        assertFalse(executed);
        assertEquals(0, executions.get());
        verify(repository, never()).release(anyString(), anyString());
    }

    @Test
    void shouldReleaseLeaseAfterSuccessfulTask() {
        JdbcLeaseLockRepository repository = mock(JdbcLeaseLockRepository.class);
        when(repository.tryAcquire(anyString(), anyString(), anyLong(), anyLong())).thenReturn(true);
        DistributedTaskLockService service = service(repository);

        assertTrue(service.runWithLock("emergency-escalation",
                Duration.ofMinutes(5), Duration.ofSeconds(55), () -> { }));

        verify(repository).release("emergency-escalation", service.instanceId());
    }

    @Test
    void shouldReleaseLeaseWhenTaskFails() {
        JdbcLeaseLockRepository repository = mock(JdbcLeaseLockRepository.class);
        when(repository.tryAcquire(anyString(), anyString(), anyLong(), anyLong())).thenReturn(true);
        DistributedTaskLockService service = service(repository);

        assertThrows(IllegalStateException.class, () -> service.runWithLock(
                "health-warning-scan", Duration.ofMinutes(70), Duration.ofMinutes(55),
                () -> { throw new IllegalStateException("test"); }));

        verify(repository).release("health-warning-scan", service.instanceId());
    }

    @Test
    void shouldUseLocalFallbackWhenDatabaseIsUnavailable() {
        JdbcLeaseLockRepository repository = mock(JdbcLeaseLockRepository.class);
        when(repository.tryAcquire(anyString(), anyString(), anyLong(), anyLong()))
                .thenThrow(new DataAccessResourceFailureException("offline"));
        DistributedTaskLockService service = service(repository);
        AtomicInteger executions = new AtomicInteger();

        assertTrue(service.runWithLock("reminder-expiration",
                Duration.ofMinutes(5), Duration.ZERO, executions::incrementAndGet));

        assertEquals(1, executions.get());
        verify(repository, never()).release(anyString(), anyString());
    }

    private DistributedTaskLockService service(JdbcLeaseLockRepository repository) {
        return new DistributedTaskLockService(repository, new SimpleMeterRegistry(), true, "test-node");
    }
}
