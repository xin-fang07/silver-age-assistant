package com.example.elderai.service;

import com.example.elderai.entity.EmergencyHelp;
import com.example.elderai.mapper.EmergencyHelpMapper;
import com.example.elderai.mapper.EmergencyNotificationMapper;
import com.example.elderai.service.impl.EmergencyNotificationServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class EmergencyNotificationServiceImplTest {

    @Test
    void shouldRecordSuccessfulEmailNotification() {
        EmergencyNotificationMapper notificationMapper = mock(EmergencyNotificationMapper.class);
        EmergencyHelpMapper helpMapper = mock(EmergencyHelpMapper.class);
        EmailService emailService = mock(EmailService.class);
        when(emailService.sendEmergencyEmail(any(), any(), any(), any(), any(), any())).thenReturn(true);

        EmergencyNotificationServiceImpl service = new EmergencyNotificationServiceImpl();
        ReflectionTestUtils.setField(service, "notificationMapper", notificationMapper);
        ReflectionTestUtils.setField(service, "helpMapper", helpMapper);
        ReflectionTestUtils.setField(service, "emailService", emailService);

        EmergencyHelp help = new EmergencyHelp();
        help.setId(10L);
        help.setContactName("家属");
        help.setContactPhone("13800001111");
        help.setContactEmail("family@example.com");
        help.setHelpContent("需要帮助");

        service.notifyCreated(help);

        assertEquals(EmergencyNotificationServiceImpl.SENT, help.getNotificationStatus());
        verify(notificationMapper, times(2)).insert(any());
        verify(helpMapper).updateById(help);
    }

    @Test
    void shouldRecordSkippedEmailWithoutAddress() {
        EmergencyNotificationMapper notificationMapper = mock(EmergencyNotificationMapper.class);
        EmergencyHelpMapper helpMapper = mock(EmergencyHelpMapper.class);
        EmailService emailService = mock(EmailService.class);

        EmergencyNotificationServiceImpl service = new EmergencyNotificationServiceImpl();
        ReflectionTestUtils.setField(service, "notificationMapper", notificationMapper);
        ReflectionTestUtils.setField(service, "helpMapper", helpMapper);
        ReflectionTestUtils.setField(service, "emailService", emailService);

        EmergencyHelp help = new EmergencyHelp();
        help.setId(11L);
        service.notifyCreated(help);

        assertEquals(EmergencyNotificationServiceImpl.NOT_CONFIGURED, help.getNotificationStatus());
        verifyNoInteractions(emailService);
        verify(notificationMapper, times(2)).insert(any());
    }
}
