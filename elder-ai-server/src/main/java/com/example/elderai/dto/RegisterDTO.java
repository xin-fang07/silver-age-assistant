package com.example.elderai.dto;

import lombok.Data;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * 用户注册请求DTO
 * 接收前端提交的注册信息
 */
@Data
public class RegisterDTO {

    /** 用户名，不能为空 */
    @NotBlank(message = "用户名不能为空")
    private String username;

    /** 密码，不能为空 */
    @NotBlank(message = "密码不能为空")
    private String password;

    /** 手机号码 */
    private String phone;

    /** 邮箱地址 */
    @Email(message = "请输入正确的邮箱格式")
    private String email;

    /**
     * 角色：FAMILY-家属（默认）。
     * ADMIN 仅后台生成，前端不可注册。ELDER 角色已废弃（老人以档案形式管理）。
     */
    private String role;
}
