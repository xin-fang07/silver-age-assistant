package com.example.elderai.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertTrue;

class HealthRecordDTOTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void shouldAcceptValidBloodPressure() {
        HealthRecordDTO dto = new HealthRecordDTO();
        dto.setBloodPressureHigh(125);
        dto.setBloodPressureLow(78);
        dto.setRecordDate(LocalDate.now());

        assertTrue(validator.validate(dto).isEmpty());
    }

    @Test
    void shouldRequireMetricAndRejectFutureDate() {
        HealthRecordDTO dto = new HealthRecordDTO();
        dto.setRecordDate(LocalDate.now().plusDays(1));

        var messages = validator.validate(dto).stream().map(v -> v.getMessage()).toList();
        assertTrue(messages.contains("请至少填写一项健康指标"));
        assertTrue(messages.contains("记录日期不能晚于今天"));
    }

    @Test
    void shouldRejectIncompleteOrReversedBloodPressure() {
        HealthRecordDTO incomplete = new HealthRecordDTO();
        incomplete.setBloodPressureHigh(120);
        incomplete.setRecordDate(LocalDate.now());
        assertTrue(validator.validate(incomplete).stream()
                .anyMatch(v -> v.getMessage().contains("必须同时填写")));

        HealthRecordDTO reversed = new HealthRecordDTO();
        reversed.setBloodPressureHigh(70);
        reversed.setBloodPressureLow(90);
        reversed.setRecordDate(LocalDate.now());
        assertTrue(validator.validate(reversed).stream()
                .anyMatch(v -> v.getMessage().contains("收缩压必须高于舒张压")));
    }
}
