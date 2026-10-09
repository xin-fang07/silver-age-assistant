package com.example.elderai.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.elderai.common.BusinessException;
import com.example.elderai.common.Result;
import com.example.elderai.entity.Device;
import com.example.elderai.entity.DevicePushLog;
import com.example.elderai.entity.DeviceSyncLog;
import com.example.elderai.entity.ElderInfo;
import com.example.elderai.entity.User;
import com.example.elderai.mapper.DeviceMapper;
import com.example.elderai.mapper.DevicePushLogMapper;
import com.example.elderai.mapper.DeviceSyncLogMapper;
import com.example.elderai.mapper.ElderInfoMapper;
import com.example.elderai.mapper.UserMapper;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/device")
public class AdminDeviceController {

    @Resource
    private DeviceMapper deviceMapper;
    @Resource
    private DeviceSyncLogMapper deviceSyncLogMapper;
    @Resource
    private DevicePushLogMapper devicePushLogMapper;
    @Resource
    private ElderInfoMapper elderInfoMapper;
    @Resource
    private UserMapper userMapper;

    @GetMapping("/elders")
    public Result<List<Map<String, Object>>> elderOptions() {
        List<ElderInfo> list = elderInfoMapper.selectList(new QueryWrapper<ElderInfo>()
                .eq("status", 1).orderByDesc("update_time"));
        List<Map<String, Object>> res = new ArrayList<>();
        for (ElderInfo e : list) {
            Map<String, Object> m = new HashMap<>();
            m.put("elderInfoId", e.getId());
            m.put("realName", e.getRealName() != null ? e.getRealName()
                    : (e.getNickname() != null ? e.getNickname() : "未命名老人"));
            res.add(m);
        }
        return Result.success(res);
    }

    @GetMapping("/families")
    public Result<List<Map<String, Object>>> familyOptions() {
        List<User> list = userMapper.selectList(new QueryWrapper<User>()
                .eq("role", "FAMILY").eq("status", 1).orderByDesc("update_time"));
        List<Map<String, Object>> res = new ArrayList<>();
        for (User u : list) {
            Map<String, Object> m = new HashMap<>();
            m.put("userId", u.getId());
            m.put("username", u.getUsername());
            m.put("nickname", u.getNickname() != null ? u.getNickname() : u.getUsername());
            res.add(m);
        }
        return Result.success(res);
    }

    @GetMapping("/list")
    public Result<List<Map<String, Object>>> list(
            @RequestParam(required = false) String deviceType,
            @RequestParam(required = false) Integer onlineStatus,
            @RequestParam(required = false) String elderName,
            @RequestParam(required = false) String keyword) {
        
        QueryWrapper<Device> wrapper = new QueryWrapper<>();
        
        if (deviceType != null && !deviceType.isEmpty()) {
            wrapper.eq("device_type", deviceType);
        }
        if (onlineStatus != null) {
            wrapper.eq("status", onlineStatus);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like("device_id", keyword)
                    .or().like("device_name", keyword)
                    .or().like("model", keyword));
        }
        
        wrapper.orderByDesc("create_time");
        
        List<Device> devices = deviceMapper.selectList(wrapper);
        List<Map<String, Object>> res = new ArrayList<>();
        
        for (Device d : devices) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", d.getId());
            m.put("deviceId", d.getDeviceId());
            m.put("deviceName", d.getDeviceName());
            m.put("deviceType", d.getDeviceType());
            m.put("model", d.getModel());
            m.put("factoryCode", d.getFactoryCode());
            m.put("firmwareVersion", d.getFirmwareVersion());
            m.put("productionBatch", d.getProductionBatch());
            m.put("vendor", d.getVendor());
            m.put("status", d.getStatus());
            m.put("enabled", d.getEnabled());
            m.put("remark", d.getRemark());
            m.put("elderId", d.getElderId());
            m.put("userId", d.getUserId());
            m.put("bindTime", d.getBindTime());
            m.put("lastSyncTime", d.getLastSyncTime());
            m.put("createTime", d.getCreateTime());
            m.put("updateTime", d.getUpdateTime());
            
            if (d.getElderId() != null) {
                ElderInfo e = elderInfoMapper.selectById(d.getElderId());
                m.put("elderName", e != null && e.getRealName() != null ? e.getRealName()
                        : (e != null && e.getNickname() != null ? e.getNickname() : "未命名老人"));
            } else {
                m.put("elderName", null);
            }
            if (d.getUserId() != null) {
                User u = userMapper.selectById(d.getUserId());
                m.put("familyUsername", u != null ? u.getUsername() : "");
            } else {
                m.put("familyUsername", "");
            }
            res.add(m);
        }
        
        return Result.success(res);
    }

    @PostMapping("/create")
    public Result<Void> create(@RequestBody Device device) {
        if (device.getDeviceId() == null || device.getDeviceId().isEmpty()) {
            throw new BusinessException(400, "设备编号不能为空");
        }
        Device exist = deviceMapper.selectOne(new QueryWrapper<Device>().eq("device_id", device.getDeviceId()));
        if (exist != null) {
            throw new BusinessException(400, "该设备编号已存在");
        }
        device.setId(null);
        device.setStatus(0);
        device.setEnabled(device.getEnabled() == null ? 1 : device.getEnabled());
        device.setCreateTime(LocalDateTime.now());
        device.setUpdateTime(LocalDateTime.now());
        deviceMapper.insert(device);
        return Result.success("设备添加成功");
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Device device) {
        Device d = deviceMapper.selectById(id);
        if (d == null) {
            throw new BusinessException(404, "未找到该设备");
        }
        if (device.getDeviceName() != null) d.setDeviceName(device.getDeviceName());
        if (device.getModel() != null) d.setModel(device.getModel());
        if (device.getFactoryCode() != null) d.setFactoryCode(device.getFactoryCode());
        if (device.getFirmwareVersion() != null) d.setFirmwareVersion(device.getFirmwareVersion());
        if (device.getProductionBatch() != null) d.setProductionBatch(device.getProductionBatch());
        if (device.getVendor() != null) d.setVendor(device.getVendor());
        if (device.getRemark() != null) d.setRemark(device.getRemark());
        d.setUpdateTime(LocalDateTime.now());
        deviceMapper.updateById(d);
        return Result.success("设备信息更新成功");
    }

    @PutMapping("/{id}/enable")
    public Result<Void> enable(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        Device d = deviceMapper.selectById(id);
        if (d == null) {
            throw new BusinessException(404, "未找到该设备");
        }
        d.setEnabled(body.get("enabled"));
        d.setUpdateTime(LocalDateTime.now());
        deviceMapper.updateById(d);
        return Result.success(body.get("enabled") == 1 ? "设备已启用" : "设备已禁用");
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Device d = deviceMapper.selectById(id);
        if (d == null) {
            throw new BusinessException(404, "未找到该设备");
        }
        deviceMapper.deleteById(id);
        return Result.success("设备已删除");
    }

    @PostMapping("/batch-import")
    public Result<Map<String, Object>> batchImport(@RequestBody List<Device> devices) {
        if (devices == null || devices.isEmpty()) {
            throw new BusinessException(400, "导入数据不能为空");
        }
        int success = 0, failed = 0;
        List<String> failedMessages = new ArrayList<>();
        for (Device device : devices) {
            try {
                if (device.getDeviceId() == null || device.getDeviceId().isEmpty()) {
                    failed++;
                    failedMessages.add("设备编号为空");
                    continue;
                }
                Device exist = deviceMapper.selectOne(new QueryWrapper<Device>().eq("device_id", device.getDeviceId()));
                if (exist != null) {
                    failed++;
                    failedMessages.add("设备编号 " + device.getDeviceId() + " 已存在");
                    continue;
                }
                device.setId(null);
                device.setStatus(0);
                device.setEnabled(device.getEnabled() == null ? 1 : device.getEnabled());
                device.setCreateTime(LocalDateTime.now());
                device.setUpdateTime(LocalDateTime.now());
                deviceMapper.insert(device);
                success++;
            } catch (Exception e) {
                failed++;
                failedMessages.add("设备编号 " + device.getDeviceId() + " 导入失败: " + e.getMessage());
            }
        }
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("failed", failed);
        result.put("failedMessages", failedMessages);
        return Result.success(result);
    }

    @PutMapping("/{id}/bind-elder")
    public Result<Void> bindElder(@PathVariable Long id, @RequestBody Map<String, Long> body) {
        Device d = deviceMapper.selectById(id);
        if (d == null) {
            throw new BusinessException(404, "未找到该设备");
        }
        Long elderId = body.get("elderId");
        if (elderId == null) {
            throw new BusinessException(400, "请选择要绑定的老人档案");
        }
        ElderInfo elder = elderInfoMapper.selectById(elderId);
        if (elder == null) {
            throw new BusinessException(404, "未找到该老人档案");
        }
        d.setElderId(elderId);
        d.setBindTime(LocalDateTime.now());
        d.setUpdateTime(LocalDateTime.now());
        deviceMapper.updateById(d);
        return Result.success("设备已绑定到老人档案");
    }

    @PutMapping("/{id}/unbind-elder")
    public Result<Void> unbindElder(@PathVariable Long id) {
        Device d = deviceMapper.selectById(id);
        if (d == null) {
            throw new BusinessException(404, "未找到该设备");
        }
        d.setElderId(null);
        d.setUpdateTime(LocalDateTime.now());
        deviceMapper.updateById(d);
        return Result.success("已解除设备与老人档案的绑定");
    }

    @PutMapping("/{id}/rebind")
    public Result<Void> rebind(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Device d = deviceMapper.selectById(id);
        if (d == null) {
            throw new BusinessException(404, "未找到该设备");
        }
        Long elderId = body.containsKey("elderId") ? ((Number) body.get("elderId")).longValue() : null;
        Long userId = body.containsKey("userId") ? ((Number) body.get("userId")).longValue() : null;
        
        if (elderId != null) {
            ElderInfo elder = elderInfoMapper.selectById(elderId);
            if (elder == null) {
                throw new BusinessException(404, "未找到该老人档案");
            }
            d.setElderId(elderId);
        }
        if (userId != null) {
            User user = userMapper.selectById(userId);
            if (user == null || !"FAMILY".equals(user.getRole())) {
                throw new BusinessException(404, "未找到该家属用户");
            }
            d.setUserId(userId);
        }
        d.setBindTime(LocalDateTime.now());
        d.setUpdateTime(LocalDateTime.now());
        deviceMapper.updateById(d);
        return Result.success("设备换绑成功");
    }

    @GetMapping("/bind-records")
    public Result<List<Map<String, Object>>> bindRecords(
            @RequestParam(required = false) Long deviceId,
            @RequestParam(required = false) Long elderId) {
        List<Device> devices = deviceMapper.selectList(new QueryWrapper<Device>().orderByDesc("bind_time"));
        List<Map<String, Object>> res = new ArrayList<>();
        for (Device d : devices) {
            if (deviceId != null && !deviceId.equals(d.getId())) continue;
            if (elderId != null && !elderId.equals(d.getElderId())) continue;
            
            Map<String, Object> m = new HashMap<>();
            m.put("id", d.getId());
            m.put("deviceId", d.getDeviceId());
            m.put("deviceType", d.getDeviceType());
            m.put("elderId", d.getElderId());
            m.put("userId", d.getUserId());
            m.put("bindTime", d.getBindTime());
            m.put("isBound", d.getElderId() != null);
            
            if (d.getElderId() != null) {
                ElderInfo e = elderInfoMapper.selectById(d.getElderId());
                m.put("elderName", e != null && e.getRealName() != null ? e.getRealName()
                        : (e != null && e.getNickname() != null ? e.getNickname() : "未命名老人"));
            } else {
                m.put("elderName", null);
            }
            if (d.getUserId() != null) {
                User u = userMapper.selectById(d.getUserId());
                m.put("familyUsername", u != null ? u.getUsername() : "");
            } else {
                m.put("familyUsername", "");
            }
            res.add(m);
        }
        return Result.success(res);
    }

    @GetMapping("/sync-logs")
    public Result<List<DeviceSyncLog>> syncLogs() {
        List<DeviceSyncLog> logs = deviceSyncLogMapper.selectList(
                new QueryWrapper<DeviceSyncLog>().orderByDesc("sync_time").last("LIMIT 200"));
        return Result.success(logs);
    }

    @GetMapping("/health-data")
    public Result<List<Map<String, Object>>> healthData(
            @RequestParam(required = false) String deviceId,
            @RequestParam(required = false) String status) {
        QueryWrapper<DeviceSyncLog> wrapper = new QueryWrapper<>();
        if (deviceId != null && !deviceId.isEmpty()) {
            wrapper.eq("device_id", deviceId);
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq("sync_status", status);
        }
        wrapper.orderByDesc("sync_time").last("LIMIT 300");
        
        List<DeviceSyncLog> logs = deviceSyncLogMapper.selectList(wrapper);
        List<Map<String, Object>> res = new ArrayList<>();
        for (DeviceSyncLog log : logs) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", log.getId());
            m.put("deviceId", log.getDeviceId());
            m.put("deviceName", log.getDeviceName());
            m.put("elderName", log.getElderName());
            m.put("metricsSummary", log.getMetricsSummary());
            m.put("syncStatus", log.getSyncStatus());
            m.put("errorMsg", log.getErrorMsg());
            m.put("syncTime", log.getSyncTime());
            m.put("createTime", log.getCreateTime());
            res.add(m);
        }
        return Result.success(res);
    }

    @GetMapping("/health-data/statistics")
    public Result<Map<String, Object>> healthDataStatistics() {
        Map<String, Object> result = new HashMap<>();
        
        long totalCount = deviceSyncLogMapper.selectCount(new QueryWrapper<>());
        long successCount = deviceSyncLogMapper.selectCount(new QueryWrapper<DeviceSyncLog>().eq("sync_status", "SUCCESS"));
        long failCount = deviceSyncLogMapper.selectCount(new QueryWrapper<DeviceSyncLog>().eq("sync_status", "FAIL"));
        
        result.put("totalCount", totalCount);
        result.put("successCount", successCount);
        result.put("failCount", failCount);
        result.put("successRate", totalCount > 0 ? (double) successCount / totalCount * 100 : 0);
        
        List<DeviceSyncLog> recentLogs = deviceSyncLogMapper.selectList(
                new QueryWrapper<DeviceSyncLog>().orderByDesc("sync_time").last("LIMIT 100"));
        
        Map<String, Long> deviceFreq = new HashMap<>();
        Map<String, Long> deviceSuccess = new HashMap<>();
        for (DeviceSyncLog log : recentLogs) {
            String key = log.getDeviceId();
            deviceFreq.merge(key, 1L, Long::sum);
            if ("SUCCESS".equals(log.getSyncStatus())) {
                deviceSuccess.merge(key, 1L, Long::sum);
            }
        }
        
        List<Map<String, Object>> deviceStats = new ArrayList<>();
        for (String deviceId : deviceFreq.keySet()) {
            Map<String, Object> m = new HashMap<>();
            m.put("deviceId", deviceId);
            m.put("reportCount", deviceFreq.get(deviceId));
            m.put("successCount", deviceSuccess.getOrDefault(deviceId, 0L));
            deviceStats.add(m);
        }
        deviceStats.sort((a, b) -> Long.compare((Long) b.get("reportCount"), (Long) a.get("reportCount")));
        result.put("deviceStats", deviceStats);
        
        return Result.success(result);
    }

    @GetMapping("/api-logs")
    public Result<List<Map<String, Object>>> apiLogs(
            @RequestParam(required = false) String deviceId,
            @RequestParam(required = false) String status) {
        QueryWrapper<DevicePushLog> wrapper = new QueryWrapper<>();
        if (deviceId != null && !deviceId.isEmpty()) {
            wrapper.eq("device_id", deviceId);
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq("status", status);
        }
        wrapper.orderByDesc("create_time").last("LIMIT 300");
        
        List<DevicePushLog> logs = devicePushLogMapper.selectList(wrapper);
        List<Map<String, Object>> res = new ArrayList<>();
        for (DevicePushLog log : logs) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", log.getId());
            m.put("deviceId", log.getDeviceId());
            m.put("pushType", log.getPushType());
            m.put("title", log.getTitle());
            m.put("content", log.getContent());
            m.put("status", log.getStatus());
            m.put("failReason", log.getFailReason());
            m.put("createTime", log.getCreateTime());
            res.add(m);
        }
        return Result.success(res);
    }

    @GetMapping("/api-logs/statistics")
    public Result<Map<String, Object>> apiLogsStatistics() {
        Map<String, Object> result = new HashMap<>();
        
        long totalCount = devicePushLogMapper.selectCount(new QueryWrapper<>());
        long successCount = devicePushLogMapper.selectCount(new QueryWrapper<DevicePushLog>().eq("status", "SUCCESS"));
        long failCount = devicePushLogMapper.selectCount(new QueryWrapper<DevicePushLog>().eq("status", "FAIL"));
        
        result.put("totalCount", totalCount);
        result.put("successCount", successCount);
        result.put("failCount", failCount);
        
        List<DevicePushLog> recentLogs = devicePushLogMapper.selectList(
                new QueryWrapper<DevicePushLog>().orderByDesc("create_time").last("LIMIT 200"));
        
        Map<String, Long> typeStats = new HashMap<>();
        Map<String, Long> failReasons = new HashMap<>();
        for (DevicePushLog log : recentLogs) {
            typeStats.merge(log.getPushType() != null ? log.getPushType() : "UNKNOWN", 1L, Long::sum);
            if ("FAIL".equals(log.getStatus()) && log.getFailReason() != null) {
                failReasons.merge(log.getFailReason(), 1L, Long::sum);
            }
        }
        
        result.put("typeStats", typeStats);
        result.put("failReasons", failReasons);
        
        return Result.success(result);
    }
}
