package com.example.elderai.service;

import com.example.elderai.entity.DevicePushLog;

import java.util.List;

public interface DevicePushLogService {
    void record(Long elderId, Long familyUserId, String deviceId, String pushType,
                Long refId, String title, String content, String status, String failReason);

    List<DevicePushLog> listAll();

    List<DevicePushLog> listByFamily(Long familyUserId);
}
