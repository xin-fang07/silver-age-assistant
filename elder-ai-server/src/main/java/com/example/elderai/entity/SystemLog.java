package com.example.elderai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统日志实体类，对应数据库 system_log 表
 * 记录系统中重要的操作行为，用于审计和问题追踪
 */
@Data
@TableName("system_log")
public class SystemLog {

    /** 主键ID，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 操作人用户ID */
    private Long userId;

    /** 操作人用户名 */
    private String username;

    /** 操作人角色：ELDER、FAMILY、ADMIN；匿名请求为空 */
    private String userRole;

    /** 操作描述，如：用户登录、新增提醒 */
    private String operation;

    /** 请求方法全限定名 */
    private String method;

    /** 请求参数JSON */
    private String params;

    /** 请求来源IP地址 */
    private String ip;

    /** 操作耗时（毫秒） */
    private Long duration;

    /** 请求追踪编号 */
    private String traceId;

    /** HTTP 状态码 */
    private Integer statusCode;

    /** 是否成功：0-失败，1-成功 */
    private Integer success;

    /** 安全错误摘要 */
    private String errorMessage;

    /** 操作时间 */
    private LocalDateTime createTime;
}
