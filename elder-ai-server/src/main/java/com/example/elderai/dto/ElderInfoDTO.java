package com.example.elderai.dto;

import lombok.Data;

/**
 * 老年人信息更新请求DTO
 * 用于更新老年人的个人资料
 */
@Data
public class ElderInfoDTO {

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
}
