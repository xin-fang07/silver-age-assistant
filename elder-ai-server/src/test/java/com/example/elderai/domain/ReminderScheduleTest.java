package com.example.elderai.domain;

import com.example.elderai.common.BusinessException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ReminderScheduleTest {

    @Test
    void shouldAdvanceDailyReminderPastReferenceTime() {
        LocalDateTime scheduled = LocalDateTime.of(2026, 7, 10, 8, 0);
        LocalDateTime reference = LocalDateTime.of(2026, 7, 12, 9, 0);

        assertEquals(LocalDateTime.of(2026, 7, 13, 8, 0),
                ReminderSchedule.nextAfter(scheduled, "DAILY", reference));
    }

    @Test
    void shouldKeepWeekdayForWeeklyReminder() {
        LocalDateTime scheduled = LocalDateTime.of(2026, 7, 16, 9, 30);
        LocalDateTime reference = LocalDateTime.of(2026, 7, 17, 10, 0);

        assertEquals(LocalDateTime.of(2026, 7, 23, 9, 30),
                ReminderSchedule.nextAfter(scheduled, "WEEKLY", reference));
    }

    @Test
    void shouldRejectNextTimeForOneOffReminder() {
        assertThrows(BusinessException.class, () -> ReminderSchedule.nextAfter(
                LocalDateTime.now(), "ONCE", LocalDateTime.now()));
    }

    @Test
    void shouldCountEveryMissedDailyOccurrence() {
        LocalDateTime scheduled = LocalDateTime.of(2026, 7, 10, 8, 0);
        LocalDateTime reference = LocalDateTime.of(2026, 7, 12, 9, 0);

        assertEquals(3, ReminderSchedule.dueOccurrences(scheduled, "DAILY", reference));
    }
}
