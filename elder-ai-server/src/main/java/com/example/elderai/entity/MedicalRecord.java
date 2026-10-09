package com.example.elderai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 医疗就诊记录实体，对应表 medical_record。
 * <p>
 * 与老人档案（elder_info）通过 elder_info_id 关联，与家属用户通过 family_user_id 关联，
 * 不再使用旧的 ELDER 用户体系。附件（病历 / 检查报告图片）以 URL 列表形式存储，
 * 文件本身由通用的文件上传接口落盘，数据库仅保存访问 URL。
 * </p>
 *
 * @author elder-ai-team
 */
@TableName(value = "medical_record", autoResultMap = true)
public class MedicalRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联老人档案 id（elder_info.id） */
    private Long elderInfoId;

    /** 关联家属用户 id（user.id，角色为 FAMILY） */
    private Long familyUserId;

    /** 就诊医院 */
    private String hospitalName;

    /** 就诊科室 */
    private String department;

    /** 主治医生 */
    private String doctorName;

    /** 诊断结论 */
    private String diagnosis;

    /** 就诊日期 */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate visitDate;

    /** 医嘱 / 处方说明 */
    private String prescription;

    /** 附件图片 URL 列表（病历、检查报告等），JSON 列自动序列化 */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> attachments;

    /** 备注 */
    private String remark;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    /** 非持久化：老人真实姓名（展示用） */
    @TableField(exist = false)
    private String elderName;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getElderInfoId() {
        return elderInfoId;
    }

    public void setElderInfoId(Long elderInfoId) {
        this.elderInfoId = elderInfoId;
    }

    public Long getFamilyUserId() {
        return familyUserId;
    }

    public void setFamilyUserId(Long familyUserId) {
        this.familyUserId = familyUserId;
    }

    public String getHospitalName() {
        return hospitalName;
    }

    public void setHospitalName(String hospitalName) {
        this.hospitalName = hospitalName;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    public LocalDate getVisitDate() {
        return visitDate;
    }

    public void setVisitDate(LocalDate visitDate) {
        this.visitDate = visitDate;
    }

    public String getPrescription() {
        return prescription;
    }

    public void setPrescription(String prescription) {
        this.prescription = prescription;
    }

    public List<String> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<String> attachments) {
        this.attachments = attachments;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public String getElderName() {
        return elderName;
    }

    public void setElderName(String elderName) {
        this.elderName = elderName;
    }
}
