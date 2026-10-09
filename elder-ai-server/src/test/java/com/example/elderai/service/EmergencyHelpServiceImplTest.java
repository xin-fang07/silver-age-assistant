package com.example.elderai.service;

import com.example.elderai.common.BusinessException;
import com.example.elderai.dto.EmergencyHelpDTO;
import com.example.elderai.entity.EmergencyHelp;
import com.example.elderai.mapper.EmergencyHelpMapper;
import com.example.elderai.service.impl.EmergencyHelpServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class EmergencyHelpServiceImplTest {

    private EmergencyHelpMapper helpMapper;
    private EmergencyNotificationService notificationService;
    private EmergencyHelpServiceImpl service;

    @BeforeEach
    void setUp() {
        helpMapper = mock(EmergencyHelpMapper.class);
        notificationService = mock(EmergencyNotificationService.class);
        service = new EmergencyHelpServiceImpl();
        ReflectionTestUtils.setField(service, "emergencyHelpMapper", helpMapper);
        ReflectionTestUtils.setField(service, "emergencyNotificationService", notificationService);
    }

    @Test
    void shouldReturnExistingHelpForRepeatedIdempotencyKey() {
        EmergencyHelp existing = new EmergencyHelp();
        existing.setId(88L);
        existing.setUserId(2L);
        existing.setRequestId("sos-request-12345678");
        when(helpMapper.selectOne(any())).thenReturn(existing);

        EmergencyHelp result = service.createHelp(2L, new EmergencyHelpDTO(), "sos-request-12345678");

        assertSame(existing, result);
        verify(helpMapper, never()).insert(any());
        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldCreateAndNotifyOnceForNewRequest() {
        when(helpMapper.selectOne(any())).thenReturn(null);
        EmergencyHelpDTO dto = new EmergencyHelpDTO();
        dto.setHelpContent("需要帮助");

        EmergencyHelp result = service.createHelp(2L, dto, "sos-request-87654321");

        assertEquals("sos-request-87654321", result.getRequestId());
        verify(helpMapper).insert(result);
        verify(notificationService).notifyCreated(result);
    }

    @Test
    void shouldRejectMalformedIdempotencyKey() {
        assertThrows(BusinessException.class,
                () -> service.createHelp(2L, new EmergencyHelpDTO(), "bad key"));
        verifyNoInteractions(helpMapper, notificationService);
    }
}
