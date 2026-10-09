package com.example.elderai.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReminderDTOTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void shouldAcceptFutureDailyReminder() {
        ReminderDTO dto = validReminder();
        dto.setRepeatType("DAILY");
        assertTrue(validator.validate(dto).isEmpty());
    }

    @Test
    void shouldRejectPastTimeAndUnknownType() {
        ReminderDTO dto = validReminder();
        dto.setRemindTime(LocalDateTime.now().minusMinutes(1));
        dto.setRemindType("UNKNOWN");
        dto.setRepeatType("MONTHLY");
        assertFalse(validator.validate(dto).isEmpty());
    }

    private ReminderDTO validReminder() {
        ReminderDTO dto = new ReminderDTO();
        dto.setTitle("吃降压药");
        dto.setRemindType("MEDICINE");
        dto.setRemindTime(LocalDateTime.now().plusHours(1));
        return dto;
    }
}
