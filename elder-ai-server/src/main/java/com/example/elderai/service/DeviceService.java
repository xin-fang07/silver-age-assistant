package com.example.elderai.service;

import com.example.elderai.dto.DeviceEmergencyDTO;
import com.example.elderai.dto.DeviceUploadDTO;
import com.example.elderai.entity.Device;
import com.example.elderai.entity.HealthRecord;

import java.util.List;
import java.util.Map;

/**
 * 智能设备服务
 * 负责家属绑定/解绑设备、查询已绑定设备列表，以及读取设备上报的健康数据。
 */
public interface DeviceService {

    /** 家属绑定一台设备 */
    void bind(Long familyUserId, Map<String, String> body);

    /** 家属解绑设备 */
    void unbind(Long familyUserId, String deviceId);

    /** 查询家属已绑定的设备列表 */
    List<Device> myDevices(Long familyUserId);

    /** 读取某设备最近上报的健康数据 */
    List<HealthRecord> deviceData(String deviceId);

    /** 模拟设备上报健康数据：写入 health_record(DEVICE) 与 device_sync_log */
    void upload(DeviceUploadDTO dto);

    /** 模拟设备上报 SOS 紧急求助：写入 emergency_help，归属指定老人 */
    void reportEmergency(DeviceEmergencyDTO dto);
}
