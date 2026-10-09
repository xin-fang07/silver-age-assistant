package com.example.elderai.controller;

import com.example.elderai.common.Result;
import com.example.elderai.dto.ForgotPasswordDTO;
import com.example.elderai.dto.LoginDTO;
import com.example.elderai.dto.RegisterDTO;
import com.example.elderai.dto.ResetPasswordDTO;
import com.example.elderai.service.PasswordResetService;
import com.example.elderai.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestHeader;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.Map;

/**
 * 认证控制器
 * <p>
 * 处理用户登录和注册请求，这两个接口无需 JWT 认证。
 * 登录成功后返回 JWT Token 和用户基本信息。
 * </p>
 *
 * @author elder-ai-team
 */
@Tag(name = "认证管理", description = "用户登录、注册接口，无需认证")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    /** 注入用户服务，处理登录和注册的业务逻辑 */
    @Resource
    private UserService userService;

    /** 注入密码重置服务 */
    @Resource
    private PasswordResetService passwordResetService;

    /**
     * 用户登录
     * <p>
     * 验证用户名和密码，成功后返回 JWT Token 及用户信息。
     * 前端收到 Token 后应存储到 localStorage，后续请求在 Authorization 请求头中携带。
     * </p>
     *
     * @param dto 登录请求参数（包含 username 和 password）
     * @return Result 对象，data 中包含 token 和 userInfo 两个字段
     */
    @Operation(summary = "用户登录", description = "验证用户名密码，返回 JWT Token 和用户信息")
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginDTO dto) {
        // 调用 userService.login() 进行身份验证，返回包含 token 和用户信息的 Map
        Map<String, Object> result = userService.login(dto);
        return Result.success("登录成功", result);
    }

    /**
     * 用户注册
     * <p>
     * 创建新用户账号，注册成功后默认角色为"老年人"（ELDER）。
     * 同时会自动创建一个空的 ElderInfo 记录与该用户关联。
     * </p>
     *
     * @param dto 注册请求参数（包含 username、password、phone）
     * @return Result 对象，操作结果提示
     */
    @Operation(summary = "用户注册", description = "创建新用户账号，支持老年用户和家属两种角色")
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterDTO dto) {
        // 调用 userService.register() 创建新用户
        userService.register(dto);
        return Result.success();
    }

    @Operation(summary = "忘记密码", description = "发送密码重置验证码到邮箱")
    @PostMapping("/forgot-password")
    public Result<Void> forgotPassword(@Valid @RequestBody ForgotPasswordDTO dto) {
        passwordResetService.sendResetCode(dto);
        return Result.success("验证码已发送到您的邮箱，请查收");
    }

    @Operation(summary = "重置密码", description = "使用验证码重置密码")
    @PostMapping("/reset-password")
    public Result<Void> resetPassword(@Valid @RequestBody ResetPasswordDTO dto) {
        passwordResetService.resetPassword(dto);
        return Result.success("密码重置成功，请登录");
    }

    @Operation(summary = "退出登录", description = "将当前 Token 加入黑名单，使其立即失效")
    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader(value = "Authorization", required = false) String authorization) {
        if (authorization != null && authorization.startsWith("Bearer ")) {
            userService.logout(authorization.substring(7).trim());
        }
        return Result.success("已退出登录");
    }
}
