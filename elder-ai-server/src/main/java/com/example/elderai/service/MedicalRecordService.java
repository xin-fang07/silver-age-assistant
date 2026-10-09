package com.example.elderai.service;

import com.example.elderai.common.PageResult;
import com.example.elderai.entity.MedicalRecord;

import java.util.List;

/**
 * 医疗就诊记录服务接口。
 *
 * @author elder-ai-team
 */
public interface MedicalRecordService {

    /** 家属端：分页查询本人绑定老人的就诊记录 */
    PageResult<MedicalRecord> pageByFamily(Long familyUserId, Long elderInfoId, Integer pageNum, Integer pageSize);

    /** 家属端：查看自己的某条记录详情（校验归属） */
    MedicalRecord detail(Long id, Long familyUserId);

    /** 家属端：新增就诊记录（记录归属家属，校验老人绑定） */
    Long create(MedicalRecord entity, Long familyUserId);

    /** 家属端：修改自己的记录（校验归属，仅覆盖传入字段） */
    void update(MedicalRecord entity, Long familyUserId);

    /** 家属端：删除自己的记录（校验归属） */
    void deleteOwn(Long id, Long familyUserId);

    /** 管理端：全量分页查询（支持按老人 / 关键字过滤） */
    PageResult<MedicalRecord> pageAll(Long elderInfoId, String keyword, Integer pageNum, Integer pageSize);

    /** 管理端：查看任意记录详情 */
    MedicalRecord adminDetail(Long id);

    /** 管理端：修改任意记录 */
    void adminUpdate(MedicalRecord entity);

    /** 管理端：删除任意记录 */
    void adminDelete(Long id);
}
