package com.example.elderai.controller;

import com.example.elderai.common.BusinessException;
import com.example.elderai.common.Result;
import com.example.elderai.dto.DeviceEmergencyDTO;
import com.example.elderai.dto.DeviceUploadDTO;
import com.example.elderai.security.SecurityUtils;
import com.example.elderai.service.DeviceService;
import com.example.elderai.service.ReminderService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 智能设备控制器
 * <p>
 * 提供家属绑定/解绑设备、查询设备列表、读取设备上报数据等接口。
 * 设备上报健康数据的接口在 HealthController（/api/health/device-data）。
 * </p>
 */
@RestController
@RequestMapping("/api/device")
public class DeviceController {

    @Resource
    private DeviceService deviceService;
    @Resource
    private ReminderService reminderService;

    /** 当前家属已绑定的设备列表 */
    @GetMapping("/elder/devices")
    public Result<List<?>> myDevices() {
        return Result.success(deviceService.myDevices(SecurityUtils.currentUserId()));
    }

    /** 绑定设备 */
    @PostMapping("/bind")
    public Result<Void> bind(@RequestBody Map<String, String> body) {
        deviceService.bind(SecurityUtils.currentUserId(), body);
        return Result.success("设备绑定成功");
    }

    /** 解绑设备 */
    @DeleteMapping("/unbind/{deviceId}")
    public Result<Void> unbind(@PathVariable String deviceId) {
        deviceService.unbind(SecurityUtils.currentUserId(), deviceId);
        return Result.success("设备解绑成功");
    }

    /** 读取某设备最近上报的健康数据 */
    @GetMapping("/data/{deviceId}")
    public Result<List<?>> deviceData(@PathVariable String deviceId) {
        return Result.success(deviceService.deviceData(deviceId));
    }

    /** 模拟设备批量上报健康数据（免登录，供外部设备 / 演示脚本调用）。 */
    @PostMapping("/upload")
    public Result<Void> upload(@RequestBody DeviceUploadDTO dto) {
        deviceService.upload(dto);
        return Result.success("设备数据上传成功");
    }

    /** 模拟将提醒推送到老人设备（虚拟推送，不接真实硬件）。 */
    @PostMapping("/push-reminder")
    public Result<Void> pushReminder(@RequestBody Map<String, Long> body) {
        Long reminderId = body.get("reminderId");
        if (reminderId == null) {
            throw new BusinessException(400, "提醒ID不能为空");
        }
        reminderService.simulatePush(reminderId);
        return Result.success("已推送到老人设备");
    }

    /** 模拟老人确认收到提醒（老人不在系统内操作，由设备端模拟确认）。 */
    @PostMapping("/confirm-reminder")
    public Result<Void> confirmReminder(@RequestBody Map<String, Long> body) {
        Long reminderId = body.get("reminderId");
        if (reminderId == null) {
            throw new BusinessException(400, "提醒ID不能为空");
        }
        reminderService.confirmByElder(reminderId);
        return Result.success("老人已确认收到提醒");
    }

    /** 模拟设备上报 SOS 紧急求助（免登录，供外部设备 / 演示脚本调用）。 */
    @PostMapping("/emergency")
    public Result<Void> reportEmergency(@RequestBody DeviceEmergencyDTO dto) {
        deviceService.reportEmergency(dto);
        return Result.success("SOS 报警已上报");
    }
}
