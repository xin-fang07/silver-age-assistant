package com.example.elderai.service;

import com.example.elderai.entity.HealthRecord;
import com.example.elderai.entity.HealthWarning;
import com.example.elderai.mapper.HealthRecordMapper;
import com.example.elderai.mapper.HealthWarningMapper;
import com.example.elderai.mapper.UserMapper;
import com.example.elderai.service.impl.HealthWarningServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class HealthWarningServiceImplTest {

    private HealthWarningMapper warningMapper;
    private HealthWarningServiceImpl service;

    @BeforeEach
    void setUp() {
        warningMapper = mock(HealthWarningMapper.class);
        service = new HealthWarningServiceImpl();
        ReflectionTestUtils.setField(service, "healthRecordMapper", mock(HealthRecordMapper.class));
        ReflectionTestUtils.setField(service, "healthWarningMapper", warningMapper);
        ReflectionTestUtils.setField(service, "userMapper", mock(UserMapper.class));
        ReflectionTestUtils.setField(service, "bpSystolicHigh", 140);
        ReflectionTestUtils.setField(service, "bpDiastolicHigh", 90);
        ReflectionTestUtils.setField(service, "bpSystolicLow", 90);
        ReflectionTestUtils.setField(service, "bpDiastolicLow", 60);
        ReflectionTestUtils.setField(service, "bloodSugarHigh", 7.0);
        ReflectionTestUtils.setField(service, "bloodSugarLow", 3.9);
        ReflectionTestUtils.setField(service, "heartRateHigh", 100);
        ReflectionTestUtils.setField(service, "heartRateLow", 60);
    }

    @Test
    void shouldCreateSevereBloodPressureWarningImmediately() {
        when(warningMapper.selectList(any())).thenReturn(List.of());
        HealthRecord record = record(190, 125);

        service.evaluateRecord(record);

        ArgumentCaptor<HealthWarning> captor = ArgumentCaptor.forClass(HealthWarning.class);
        verify(warningMapper).insert(captor.capture());
        assertEquals(3, captor.getValue().getWarningLevel());
        assertEquals(0, captor.getValue().getStatus());
        assertEquals("BLOOD_PRESSURE", captor.getValue().getWarningType());
    }

    @Test
    void shouldResolveExistingWarningWhenRecordReturnsToRange() {
        HealthWarning warning = new HealthWarning();
        warning.setId(9L);
        warning.setUserId(2L);
        warning.setRecordId(8L);
        warning.setWarningType("BLOOD_PRESSURE");
        warning.setStatus(0);
        when(warningMapper.selectList(any())).thenReturn(List.of(warning));

        service.evaluateRecord(record(125, 78));

        verify(warningMapper).updateById(warning);
        assertEquals(2, warning.getStatus());
        assertEquals(1, warning.getIsRead());
    }

    private HealthRecord record(int high, int low) {
        HealthRecord record = new HealthRecord();
        record.setId(8L);
        record.setUserId(2L);
        record.setBloodPressureHigh(high);
        record.setBloodPressureLow(low);
        return record;
    }
}
