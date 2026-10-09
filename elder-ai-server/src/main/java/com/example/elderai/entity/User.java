package com.example.elderai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体类，对应数据库 user 表
 * 角色分为 ELDER（老年人）、FAMILY（家属）和 ADMIN（管理员）三种
 */
@Data
@TableName("user")
public class User {

    /** 用户主键ID，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户名，用于登录 */
    private String username;

    /** 登录密码 */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    /** 角色：FAMILY-家属, ADMIN-管理员（ELDER 已废弃，老人改以档案形式管理） */
    private String role;

    /** 手机号码 */
    private String phone;

    /** 邮箱地址 */
    private String email;

    /** 头像URL */
    private String avatar;

    /** 用户昵称 */
    private String nickname;

    /** 状态：0-禁用, 1-正常 */
    private Integer status;
    private LocalDateTime lastLoginTime;
    private String disabledReason;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
