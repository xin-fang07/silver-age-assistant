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
@RequestMapping("/api/activity")
public class ActivityController {

    @Resource
    private ActivityService activityService;

    /** 已发布活动列表（可按类型 / 关键词筛选） */
    @GetMapping("/list")
    public PageResult<Activity> list(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return activityService.familyList(SecurityUtils.currentUserId(), type, keyword, pageNum, pageSize);
    }

    /** 活动详情 */
    @GetMapping("/{id}")
    public Result<Activity> detail(@PathVariable Long id) {
        return Result.success(activityService.familyDetail(id));
    }

    /** 家属为老人报名线下活动 */
    @PostMapping("/signup")
    public Result<Long> signup(@RequestBody ActivitySignup signup) {
        Long id = activityService.signup(signup.getActivityId(), SecurityUtils.currentUserId(),
                signup.getElderInfoId(), signup.getContactPhone(), signup.getRemark());
        return Result.success(id);
    }

    /** 我的报名记录 */
    @GetMapping("/my-signups")
    public PageResult<ActivitySignup> mySignups(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return activityService.mySignups(SecurityUtils.currentUserId(), pageNum, pageSize);
    }

    /** 取消我的报名 */
    @DeleteMapping("/signup/{signupId}")
    public Result<Void> cancelSignup(@PathVariable Long signupId) {
        activityService.cancelSignup(signupId, SecurityUtils.currentUserId());
        return Result.success();
    }
}
