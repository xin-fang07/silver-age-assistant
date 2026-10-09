package com.example.elderai.controller;

import com.example.elderai.common.PageResult;
import com.example.elderai.common.Result;
import com.example.elderai.entity.Activity;
import com.example.elderai.entity.ActivitySignup;
import com.example.elderai.security.SecurityUtils;
import com.example.elderai.service.ActivityService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/activity")
public class AdminActivityController {

    @Resource
    private ActivityService activityService;

    /** 活动全量列表 */
    @GetMapping("/list")
    public PageResult<Activity> list(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return activityService.adminList(type, keyword, pageNum, pageSize);
    }

    @GetMapping("/{id}")
    public Result<Activity> detail(@PathVariable Long id) {
        return Result.success(activityService.adminDetail(id));
    }

    /** 新增活动 */
    @PostMapping
    public Result<Long> create(@RequestBody Activity entity) {
        return Result.success(activityService.create(entity, SecurityUtils.currentUserId()));
    }

    /** 更新活动 */
    @PutMapping
    public Result<Void> update(@RequestBody Activity entity) {
        activityService.update(entity);
        return Result.success();
    }

    /** 删除活动 */
    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        activityService.remove(id);
        return Result.success();
    }

    /** 某活动的报名记录 */
    @GetMapping("/{id}/signups")
    public PageResult<ActivitySignup> signups(@PathVariable Long id,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return activityService.adminSignups(id, pageNum, pageSize);
    }

    /** 调整报名状态 */
    @PutMapping("/signups/{signupId}")
    public Result<Void> updateSignup(@PathVariable Long signupId,
            @RequestParam Integer status) {
        activityService.updateSignup(signupId, status);
        return Result.success();
    }
}
