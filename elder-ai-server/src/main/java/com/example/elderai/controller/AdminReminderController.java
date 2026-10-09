package com.example.elderai.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.elderai.common.Result;
import com.example.elderai.dto.ReminderDTO;
import com.example.elderai.entity.Reminder;
import com.example.elderai.entity.ReminderExecution;
import com.example.elderai.entity.ElderInfo;
import com.example.elderai.entity.FamilyBinding;
import com.example.elderai.mapper.ReminderMapper;
import com.example.elderai.mapper.ReminderExecutionMapper;
import com.example.elderai.mapper.ElderInfoMapper;
import com.example.elderai.mapper.FamilyBindingMapper;
import com.example.elderai.service.ReminderService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/reminders")
public class AdminReminderController {

    @Resource
    private ReminderMapper reminderMapper;

    @Resource
    private ReminderExecutionMapper executionMapper;

    @Resource
    private ElderInfoMapper elderInfoMapper;

    @Resource
    private FamilyBindingMapper familyBindingMapper;

    @Resource
    private ReminderService reminderService;

    @GetMapping
    public Result<IPage<Reminder>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String remindType,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long elderInfoId,
            @RequestParam(required = false) String pushStatus) {

        Page<Reminder> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Reminder> wrapper = new LambdaQueryWrapper<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w.like(Reminder::getTitle, keyword));
        }
        if (remindType != null && !remindType.trim().isEmpty()) {
            wrapper.eq(Reminder::getRemindType, remindType);
        }
        if (status != null) {
            wrapper.eq(Reminder::getStatus, status);
        }
        if (elderInfoId != null) {
            wrapper.eq(Reminder::getElderInfoId, elderInfoId);
        }
        if (pushStatus != null && !pushStatus.trim().isEmpty()) {
            wrapper.eq(Reminder::getPushStatus, pushStatus);
        }

        wrapper.orderByDesc(Reminder::getCreateTime);
        IPage<Reminder> result = reminderMapper.selectPage(page, wrapper);

        for (Reminder r : result.getRecords()) {
            if (r.getElderInfoId() != null) {
                ElderInfo elder = elderInfoMapper.selectById(r.getElderInfoId());
                if (elder != null) {
                    r.setElderName(elder.getRealName());
                }
            }
        }

        return Result.success(result);
    }

    @PostMapping
    public Result<Reminder> create(@RequestBody ReminderDTO dto) {
        if (dto.getElderInfoId() == null) {
            return Result.error(400, "请指定老人");
        }
        Long familyUserId = null;
        List<FamilyBinding> bindings = familyBindingMapper.selectList(
                new LambdaQueryWrapper<FamilyBinding>().eq(FamilyBinding::getElderInfoId, dto.getElderInfoId()));
        if (!bindings.isEmpty()) {
            familyUserId = bindings.get(0).getFamilyUserId();
        }
        if (familyUserId == null) {
            return Result.error(400, "老人未绑定家属");
        }

        Reminder reminder = new Reminder();
        reminder.setUserId(familyUserId);
        reminder.setFamilyUserId(familyUserId);
        reminder.setElderInfoId(dto.getElderInfoId());
        reminder.setTitle(dto.getTitle());
        reminder.setContent(dto.getContent());
        reminder.setRemindType(dto.getRemindType() != null ? dto.getRemindType() : "MEDICINE");
        reminder.setRemindTime(dto.getRemindTime());
        reminder.setRepeatType(dto.getRepeatType() != null ? dto.getRepeatType() : "DAILY");
        reminder.setStatus(0);
        reminder.setCompletedCount(0);
        reminder.setMissedCount(0);
        reminder.setConsecutiveMissedCount(0);
        reminder.setCreateTime(java.time.LocalDateTime.now());
        reminder.setUpdateTime(java.time.LocalDateTime.now());
        reminderMapper.insert(reminder);

        ElderInfo elder = elderInfoMapper.selectById(dto.getElderInfoId());
        if (elder != null) {
            reminder.setElderName(elder.getRealName());
        }

        return Result.success("提醒创建成功", reminder);
    }

    @PutMapping("/{id}")
    public Result<Reminder> update(@PathVariable Long id, @RequestBody Map<String, Object> data) {
        Reminder reminder = reminderMapper.selectById(id);
        if (reminder == null) {
            return Result.error(404, "提醒不存在");
        }

        if (data.containsKey("title")) {
            reminder.setTitle(data.get("title").toString());
        }
        if (data.containsKey("content")) {
            reminder.setContent(data.get("content").toString());
        }
        if (data.containsKey("remindType")) {
            reminder.setRemindType(data.get("remindType").toString());
        }
        if (data.containsKey("remindTime")) {
            reminder.setRemindTime(java.time.LocalDateTime.parse(data.get("remindTime").toString()));
        }
        if (data.containsKey("repeatType")) {
            reminder.setRepeatType(data.get("repeatType").toString());
        }
        if (data.containsKey("status")) {
            reminder.setStatus(Integer.parseInt(data.get("status").toString()));
        }
        reminder.setUpdateTime(java.time.LocalDateTime.now());
        reminderMapper.updateById(reminder);

        return Result.success("提醒更新成功", reminder);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Reminder reminder = reminderMapper.selectById(id);
        if (reminder == null) {
            return Result.error(404, "提醒不存在");
        }
        reminderMapper.deleteById(id);
        return Result.success("提醒删除成功");
    }

    @GetMapping("/{id}/executions")
    public Result<List<ReminderExecution>> getExecutions(@PathVariable Long id) {
        List<ReminderExecution> list = executionMapper.selectList(
                new LambdaQueryWrapper<ReminderExecution>()
                        .eq(ReminderExecution::getReminderId, id)
                        .orderByDesc(ReminderExecution::getScheduledTime));
        return Result.success(list);
    }
}