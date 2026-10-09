package com.example.elderai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.elderai.common.BusinessException;
import com.example.elderai.dto.DeviceEmergencyDTO;
import com.example.elderai.dto.DeviceUploadDTO;
import com.example.elderai.entity.Device;
import com.example.elderai.entity.DeviceSyncLog;
import com.example.elderai.entity.ElderInfo;
import com.example.elderai.entity.EmergencyHelp;
import com.example.elderai.entity.FamilyBinding;
import com.example.elderai.entity.HealthRecord;
import com.example.elderai.mapper.DeviceMapper;
import com.example.elderai.mapper.DeviceSyncLogMapper;
import com.example.elderai.mapper.ElderInfoMapper;
import com.example.elderai.mapper.EmergencyHelpMapper;
import com.example.elderai.mapper.FamilyBindingMapper;
import com.example.elderai.mapper.HealthRecordMapper;
import com.example.elderai.service.DeviceService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 智能设备服务实现
 * 负责家属绑定 / 解绑设备、查询已绑定设备列表，以及读取设备上报的健康数据。
 * 设备上报健康数据的入口在 HealthController（/api/health/device-data），
 * 上报的数据写入 health_record（source_type = DEVICE，source_device_id = 设备编号）。
 */
@Service
public class DeviceServiceImpl implements DeviceService {

    @Resource
    private DeviceMapper deviceMapper;
    @Resource
    private HealthRecordMapper healthRecordMapper;
    @Resource
    private DeviceSyncLogMapper deviceSyncLogMapper;
    @Resource
    private ElderInfoMapper elderInfoMapper;
    @Resource
    private FamilyBindingMapper familyBindingMapper;
    @Resource
    private EmergencyHelpMapper emergencyHelpMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bind(Long familyUserId, Map<String, String> body) {
        String deviceId = body.get("deviceId");
        if (deviceId == null || deviceId.isBlank()) {
            throw new BusinessException(400, "请填写设备编号");
        }
        deviceId = deviceId.trim();

        Device existing = deviceMapper.selectOne(new QueryWrapper<Device>()
                .eq("user_id", familyUserId).eq("device_id", deviceId));
        if (existing != null) {
            throw new BusinessException(400, "该设备已绑定，请勿重复绑定");
        }

        Device d = new Device();
        d.setUserId(familyUserId);
        d.setDeviceId(deviceId);
        d.setDeviceName(body.getOrDefault("deviceName", deviceId));
        d.setDeviceType(body.get("deviceType"));
        d.setVendor(body.get("vendor"));
        String elderIdStr = body.get("elderId");
        if (elderIdStr != null && !elderIdStr.isBlank()) {
            try {
                d.setElderId(Long.parseLong(elderIdStr.trim()));
            } catch (NumberFormatException e) {
                throw new BusinessException(400, "老人档案ID格式不正确");
            }
        }
        d.setStatus(1);
        d.setBindTime(LocalDateTime.now());
        d.setCreateTime(LocalDateTime.now());
        d.setUpdateTime(LocalDateTime.now());
        deviceMapper.insert(d);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unbind(Long familyUserId, String deviceId) {
        Device existing = deviceMapper.selectOne(new QueryWrapper<Device>()
                .eq("user_id", familyUserId).eq("device_id", deviceId));
        if (existing == null) {
            throw new BusinessException(404, "未找到该绑定设备");
        }
        deviceMapper.deleteById(existing.getId());
    }

    @Override
    public List<Device> myDevices(Long familyUserId) {
        return deviceMapper.selectList(new QueryWrapper<Device>()
                .eq("user_id", familyUserId).orderByDesc("bind_time"));
    }

    @Override
    public List<HealthRecord> deviceData(String deviceId) {
        return healthRecordMapper.selectList(new QueryWrapper<HealthRecord>()
                .eq("source_device_id", deviceId)
                .eq("source_type", "DEVICE")
                .orderByDesc("measured_at")
                .last("LIMIT 50"));
    }

    /**
     * 模拟设备上报健康数据（入方向）。
     * 演示用：由外部设备 / 模拟脚本调用，将体征写入 health_record(source_type=DEVICE)，
     * 同时留一条 device_sync_log 同步记录，并更新设备最后同步时间。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void upload(DeviceUploadDTO dto) {
        Long elderId = dto.getElderId();
        ElderInfo elder = elderInfoMapper.selectById(elderId);
        if (elder == null) {
            throw new BusinessException(404, "未找到该老人档案");
        }

        Device device = deviceMapper.selectOne(new QueryWrapper<Device>()
                .eq("elder_id", elderId).last("LIMIT 1"));
        String deviceId = device != null ? device.getDeviceId() : "SIM-DEVICE-DEFAULT";
        String deviceName = device != null ? device.getDeviceName() : "模拟智能设备";

        Long familyUserId = device != null ? device.getUserId() : null;
        if (familyUserId == null) {
            FamilyBinding binding = familyBindingMapper.selectOne(new QueryWrapper<FamilyBinding>()
                    .eq("elder_info_id", elderId).eq("status", 1).last("LIMIT 1"));
            familyUserId = binding != null ? binding.getFamilyUserId() : null;
        }
        if (familyUserId == null) {
            throw new BusinessException(400, "无法归属该健康数据的家属，请先为老人绑定家属或设备");
        }

        LocalDateTime now = LocalDateTime.now();
        HealthRecord hr = new HealthRecord();
        hr.setUserId(familyUserId);
        hr.setElderInfoId(elderId);
        hr.setHeartRate(dto.getHeartRate());
        if (dto.getBloodPressure() != null && !dto.getBloodPressure().isBlank()) {
            String[] bp = dto.getBloodPressure().split("/");
            try {
                if (bp.length >= 2) {
                    hr.setBloodPressureHigh(Integer.parseInt(bp[0].trim()));
                    hr.setBloodPressureLow(Integer.parseInt(bp[1].trim()));
                } else {
                    hr.setBloodPressureHigh(Integer.parseInt(bp[0].trim()));
                }
            } catch (NumberFormatException ignored) {
                // 血压格式非法，忽略
            }
        }
        hr.setBloodOxygen(dto.getBloodOxygen());
        hr.setBloodSugar(dto.getBloodSugar());
        hr.setSteps(dto.getSteps());
        hr.setMeasuredAt(now);
        hr.setRecordDate(now.toLocalDate());
        hr.setSourceType("DEVICE");
        hr.setSourceDeviceId(deviceId);
        healthRecordMapper.insert(hr);

        DeviceSyncLog log = new DeviceSyncLog();
        log.setDeviceId(deviceId);
        log.setDeviceName(deviceName);
        log.setElderInfoId(elderId);
        log.setElderName(elder.getRealName() != null ? elder.getRealName()
                : (elder.getNickname() != null ? elder.getNickname() : "未命名老人"));
        log.setFamilyUserId(familyUserId);
        log.setMetricsSummary(buildSummary(dto));
        log.setSyncStatus("SUCCESS");
        log.setSyncTime(now);
        log.setCreateTime(now);
        deviceSyncLogMapper.insert(log);

        if (device != null) {
            device.setLastSyncTime(now);
            device.setUpdateTime(now);
            deviceMapper.updateById(device);
        }
    }

    private String buildSummary(DeviceUploadDTO dto) {
        StringBuilder sb = new StringBuilder();
        if (dto.getHeartRate() != null) sb.append("心率").append(dto.getHeartRate()).append(" ");
        if (dto.getBloodPressure() != null && !dto.getBloodPressure().isBlank())
            sb.append("血压").append(dto.getBloodPressure()).append(" ");
        if (dto.getBloodOxygen() != null) sb.append("血氧").append(dto.getBloodOxygen()).append("% ");
        if (dto.getBloodSugar() != null) sb.append("血糖").append(dto.getBloodSugar()).append(" ");
        if (dto.getSteps() != null) sb.append("步数").append(dto.getSteps());
        return sb.toString().trim();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reportEmergency(DeviceEmergencyDTO dto) {
        Long elderId = dto.getElderId();
        ElderInfo elder = elderInfoMapper.selectById(elderId);
        if (elder == null) {
            throw new BusinessException(404, "未找到该老人档案");
        }

        Device device = deviceMapper.selectOne(new QueryWrapper<Device>()
                .eq("elder_id", elderId).last("LIMIT 1"));
        Long familyUserId = device != null ? device.getUserId() : null;
        if (familyUserId == null) {
            FamilyBinding binding = familyBindingMapper.selectOne(new QueryWrapper<FamilyBinding>()
                    .eq("elder_info_id", elderId).eq("status", 1).last("LIMIT 1"));
            familyUserId = binding != null ? binding.getFamilyUserId() : null;
        }

        LocalDateTime now = LocalDateTime.now();
        EmergencyHelp help = new EmergencyHelp();
        // emergency_help.user_id 承载"老人标识"，家属端按 elderInfoId(=elder_info_id) 查询，
        // 故此处填 elderId 以对齐家属端查询语义。
        help.setUserId(elderId);
        help.setContactName(elder.getEmergencyContact() != null ? elder.getEmergencyContact() : "家属");
        help.setContactPhone(elder.getEmergencyPhone());
        help.setHelpContent(dto.getContent() != null && !dto.getContent().isBlank()
                ? dto.getContent() : "老人触发 SOS 紧急求助，请家属立即响应");
        help.setStatus(0);
        help.setNotificationStatus(0);
        help.setLatitude(dto.getLatitude());
        help.setLongitude(dto.getLongitude());
        help.setLocationText(dto.getLocationText());
        help.setCreateTime(now);
        help.setUpdateTime(now);
        emergencyHelpMapper.insert(help);
    }
}
