package com.example.elderai.controller;

import com.example.elderai.common.Result;
import com.example.elderai.dto.ElderInfoDTO;
import com.example.elderai.dto.PasswordUpdateDTO;
import com.example.elderai.service.UserService;
import com.example.elderai.security.SecurityUtils;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.Map;

/**
 * 用户控制器
 * <p>
 * 处理当前登录用户的个人信息查询、密码修改和老年人资料更新。
 * 所有接口均需要 JWT 认证，从请求头中提取 Token 解析用户身份。
 * </p>
 *
 * @author elder-ai-team
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    /** 注入用户服务，处理用户信息相关的业务逻辑 */
    @Resource
    private UserService userService;

    /**
     * 从 HTTP 请求头中提取 JWT Token 并解析出当前登录用户的 ID
     * <p>
     * Token 存放在 Authorization 请求头中，格式为 "Bearer <token>"，
     * 需要先去掉 "Bearer " 前缀再交给 JwtUtils 解析。
     * </p>
     *
     * @return 当前登录用户的 ID
     */
    private Long getCurrentUserId() {
        return SecurityUtils.currentUserId();
    }

    /**
     * 获取当前用户信息
     * <p>
     * 返回用户基本信息（User 表）以及关联的老年人详细信息（ElderInfo 表）。
     * 返回的 JSON 中 user 和 elderInfo 是两个同级字段，前端可合并展示。
     * </p>
     *
     * @return Result 对象，data 中包含 user（用户基本信息）和 elderInfo（老人详细信息）
     */
    @GetMapping("/info")
    public Result<Map<String, Object>> getUserInfo() {
        Long userId = getCurrentUserId();
        // 调用 userService.getUserInfo() 获取合并后的用户信息
        Map<String, Object> info = userService.getUserInfo(userId);
        return Result.success(info);
    }

    /**
     * 修改密码
     * <p>
     * 验证旧密码正确后更新为新密码，新密码会经过 BCrypt 加密后存储。
     * 修改成功后旧密码立即失效，前端应引导用户重新登录。
     * </p>
     *
     * @param dto 密码修改请求（包含 oldPassword 和 newPassword）
     * @return Result 对象，操作结果提示
     */
    @PutMapping("/password")
    public Result<Void> updatePassword(@Valid @RequestBody PasswordUpdateDTO dto) {
        Long userId = getCurrentUserId();
        // 调用 userService.updatePassword() 验证旧密码并更新新密码
        userService.updatePassword(userId, dto);
        return Result.success();
    }

    /**
     * 更新老年人详细信息
     * <p>
     * 根据 DTO 中的非空字段更新 ElderInfo 表中的对应记录。
     * 仅更新提交的字段，未提交的字段保持原值不变（部分更新）。
     * </p>
     *
     * @param dto 老年人信息更新请求（包含 realName、gender、age、address 等字段）
     * @return Result 对象，操作结果提示
     */
    @PutMapping("/elder-info")
    public Result<Void> updateElderInfo(@Valid @RequestBody ElderInfoDTO dto) {
        Long userId = getCurrentUserId();
        userService.updateElderInfo(userId, dto);
        return Result.success();
    }

    /**
     * 更新用户基本信息（头像、昵称、电话）
     * <p>
     * 更新 User 表中的基本信息字段。
     * </p>
     *
     * @param data 用户基本信息（包含 avatar、nickname、phone）
     * @return Result 对象，操作结果提示
     */
    @PutMapping("/profile")
    public Result<Void> updateProfile(@RequestBody Map<String, Object> data) {
        Long userId = getCurrentUserId();
        userService.updateProfile(userId, data);
        return Result.success();
    }
}
