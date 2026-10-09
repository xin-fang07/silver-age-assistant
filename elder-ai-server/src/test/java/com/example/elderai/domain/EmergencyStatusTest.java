package com.example.elderai.domain;

import com.example.elderai.common.BusinessException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EmergencyStatusTest {

    @Test
    void shouldAllowNormalWorkflow() {
        assertDoesNotThrow(() -> EmergencyStatus.requireTransition(
                EmergencyStatus.SUBMITTED, EmergencyStatus.ACKNOWLEDGED));
        assertDoesNotThrow(() -> EmergencyStatus.requireTransition(
                EmergencyStatus.ACKNOWLEDGED, EmergencyStatus.PROCESSING));
        assertDoesNotThrow(() -> EmergencyStatus.requireTransition(
                EmergencyStatus.PROCESSING, EmergencyStatus.COMPLETED));
    }

    @Test
    void shouldRejectReopeningCompletedHelp() {
        assertThrows(BusinessException.class, () -> EmergencyStatus.requireTransition(
                EmergencyStatus.COMPLETED, EmergencyStatus.PROCESSING));
    }

    @Test
    void shouldOnlyTreatCompletedAndCancelledAsTerminal() {
        assertTrue(EmergencyStatus.isTerminal(EmergencyStatus.COMPLETED));
        assertTrue(EmergencyStatus.isTerminal(EmergencyStatus.CANCELLED));
        assertFalse(EmergencyStatus.isTerminal(EmergencyStatus.ESCALATED));
    }
}
