package com.example.elderai.service;

import com.example.elderai.entity.EmergencyHelp;

/** 记录并发送紧急求助通知。 */
public interface EmergencyNotificationService {
    void notifyCreated(EmergencyHelp help);
    void recordEscalation(EmergencyHelp help);
    void notifyEscalation(EmergencyHelp help);
}
