package com.example.elderai.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * 密码修改请求DTO
 * 接收用户修改密码时提交的旧密码和新密码
 */
@Data
public class PasswordUpdateDTO {

    /** 旧密码，不能为空 */
    @NotBlank(message = "旧密码不能为空")
    private String oldPassword;

    /** 新密码，不能为空 */
    @NotBlank(message = "新密码不能为空")
    private String newPassword;
}
