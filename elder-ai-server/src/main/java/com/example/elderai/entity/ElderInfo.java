package com.example.elderai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 老年人详细信息实体类，对应数据库 elder_info 表
 * 存储老年人的个人资料、紧急联系人和健康史等信息
 */
@Data
@TableName("elder_info")
public class ElderInfo {

    /** 主键ID，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 真实姓名 */
    private String realName;

    /** 性别：0-女, 1-男 */
    private Integer gender;

    /** 年龄 */
    private Integer age;

    /** 居住地址 */
    private String address;

    /** 紧急联系人姓名 */
    private String emergencyContact;

    /** 紧急联系人电话 */
    private String emergencyPhone;

    /** 身高（cm） */
    private Double height;

    /** 昵称 */
    private String nickname;

    /** 既往病史 */
    private String medicalHistory;

    /** 体重（kg） */
    private java.math.BigDecimal weight;

    /** 出生日期 */
    private java.time.LocalDate birthDate;

    /** 身份证号 */
    private String idCard;

    /** 头像URL */
    private String avatar;

    /** 血型 */
    private String bloodType;

    /** 健康状态：0-健康 1-慢性病 2-需照护 3-其他疾病 */
    private Integer healthStatus;

    /** 档案状态：1-正常 0-已注销 */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
