package com.example.elderai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.elderai.entity.DevicePushLog;
import com.example.elderai.mapper.DevicePushLogMapper;
import com.example.elderai.service.DevicePushLogService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DevicePushLogServiceImpl implements DevicePushLogService {

    @Resource
    private DevicePushLogMapper devicePushLogMapper;

    @Override
    public void record(Long elderId, Long familyUserId, String deviceId, String pushType,
                       Long refId, String title, String content, String status, String failReason) {
        DevicePushLog log = new DevicePushLog();
        log.setElderId(elderId);
        log.setFamilyUserId(familyUserId);
        log.setDeviceId(deviceId);
        log.setPushType(pushType);
        log.setRefId(refId);
        log.setTitle(title);
        log.setContent(content);
        log.setStatus(status);
        log.setFailReason(failReason);
        log.setCreateTime(LocalDateTime.now());
        devicePushLogMapper.insert(log);
    }

    @Override
    public List<DevicePushLog> listAll() {
        return devicePushLogMapper.selectList(
                new QueryWrapper<DevicePushLog>().orderByDesc("create_time").last("LIMIT 300"));
    }

    @Override
    public List<DevicePushLog> listByFamily(Long familyUserId) {
        return devicePushLogMapper.selectList(new QueryWrapper<DevicePushLog>()
                .eq("family_user_id", familyUserId).orderByDesc("create_time").last("LIMIT 300"));
    }
}
