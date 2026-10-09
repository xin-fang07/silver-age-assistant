package com.example.elderai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.elderai.common.BusinessException;
import com.example.elderai.common.PageResult;
import com.example.elderai.entity.ElderInfo;
import com.example.elderai.entity.FamilyBinding;
import com.example.elderai.entity.MedicalRecord;
import com.example.elderai.mapper.ElderInfoMapper;
import com.example.elderai.mapper.FamilyBindingMapper;
import com.example.elderai.mapper.MedicalRecordMapper;
import com.example.elderai.service.MedicalRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * 医疗就诊记录服务实现类。
 *
 * @author elder-ai-team
 */
@Service
public class MedicalRecordServiceImpl implements MedicalRecordService {

    @Autowired
    private MedicalRecordMapper medicalRecordMapper;

    @Autowired
    private ElderInfoMapper elderInfoMapper;

    @Autowired
    private FamilyBindingMapper familyBindingMapper;

    @Override
    public PageResult<MedicalRecord> pageByFamily(Long familyUserId, Long elderInfoId, Integer pageNum, Integer pageSize) {
        Page<MedicalRecord> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<MedicalRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MedicalRecord::getFamilyUserId, familyUserId);
        if (elderInfoId != null) {
            wrapper.eq(MedicalRecord::getElderInfoId, elderInfoId);
        }
        wrapper.orderByDesc(MedicalRecord::getVisitDate);
        wrapper.orderByDesc(MedicalRecord::getCreateTime);
        Page<MedicalRecord> resultPage = medicalRecordMapper.selectPage(page, wrapper);
        resultPage.getRecords().forEach(this::fillElderName);
        return PageResult.pageSuccess(resultPage.getRecords(), resultPage.getTotal(),
                resultPage.getCurrent(), resultPage.getSize());
    }

    @Override
    public MedicalRecord detail(Long id, Long familyUserId) {
        MedicalRecord entity = medicalRecordMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(404, "就诊记录不存在");
        }
        if (!familyUserId.equals(entity.getFamilyUserId())) {
            throw new BusinessException(403, "无权访问该就诊记录");
        }
        fillElderName(entity);
        return entity;
    }

    @Override
    public Long create(MedicalRecord entity, Long familyUserId) {
        if (entity.getElderInfoId() == null) {
            throw new BusinessException(400, "请选择关联的老人");
        }
        assertBound(familyUserId, entity.getElderInfoId());
        entity.setFamilyUserId(familyUserId);
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        medicalRecordMapper.insert(entity);
        return entity.getId();
    }

    @Override
    public void update(MedicalRecord entity, Long familyUserId) {
        MedicalRecord existing = medicalRecordMapper.selectById(entity.getId());
        if (existing == null) {
            throw new BusinessException(404, "就诊记录不存在");
        }
        if (!familyUserId.equals(existing.getFamilyUserId())) {
            throw new BusinessException(403, "无权修改该就诊记录");
        }
        if (entity.getHospitalName() != null) {
            existing.setHospitalName(entity.getHospitalName());
        }
        if (entity.getDepartment() != null) {
            existing.setDepartment(entity.getDepartment());
        }
        if (entity.getDoctorName() != null) {
            existing.setDoctorName(entity.getDoctorName());
        }
        if (entity.getDiagnosis() != null) {
            existing.setDiagnosis(entity.getDiagnosis());
        }
        if (entity.getVisitDate() != null) {
            existing.setVisitDate(entity.getVisitDate());
        }
        if (entity.getPrescription() != null) {
            existing.setPrescription(entity.getPrescription());
        }
        if (entity.getAttachments() != null) {
            existing.setAttachments(entity.getAttachments());
        }
        if (entity.getRemark() != null) {
            existing.setRemark(entity.getRemark());
        }
        existing.setUpdateTime(LocalDateTime.now());
        medicalRecordMapper.updateById(existing);
    }

    @Override
    public void deleteOwn(Long id, Long familyUserId) {
        MedicalRecord existing = medicalRecordMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(404, "就诊记录不存在");
        }
        if (!familyUserId.equals(existing.getFamilyUserId())) {
            throw new BusinessException(403, "无权删除该就诊记录");
        }
        medicalRecordMapper.deleteById(id);
    }

    @Override
    public PageResult<MedicalRecord> pageAll(Long elderInfoId, String keyword, Integer pageNum, Integer pageSize) {
        Page<MedicalRecord> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<MedicalRecord> wrapper = new LambdaQueryWrapper<>();
        if (elderInfoId != null) {
            wrapper.eq(MedicalRecord::getElderInfoId, elderInfoId);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(MedicalRecord::getHospitalName, keyword)
                    .or().like(MedicalRecord::getDepartment, keyword)
                    .or().like(MedicalRecord::getDiagnosis, keyword));
        }
        wrapper.orderByDesc(MedicalRecord::getVisitDate);
        wrapper.orderByDesc(MedicalRecord::getCreateTime);
        Page<MedicalRecord> resultPage = medicalRecordMapper.selectPage(page, wrapper);
        resultPage.getRecords().forEach(this::fillElderName);
        return PageResult.pageSuccess(resultPage.getRecords(), resultPage.getTotal(),
                resultPage.getCurrent(), resultPage.getSize());
    }

    @Override
    public MedicalRecord adminDetail(Long id) {
        MedicalRecord entity = medicalRecordMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(404, "就诊记录不存在");
        }
        fillElderName(entity);
        return entity;
    }

    @Override
    public void adminUpdate(MedicalRecord entity) {
        MedicalRecord existing = medicalRecordMapper.selectById(entity.getId());
        if (existing == null) {
            throw new BusinessException(404, "就诊记录不存在");
        }
        if (entity.getElderInfoId() != null) {
            existing.setElderInfoId(entity.getElderInfoId());
        }
        if (entity.getHospitalName() != null) {
            existing.setHospitalName(entity.getHospitalName());
        }
        if (entity.getDepartment() != null) {
            existing.setDepartment(entity.getDepartment());
        }
        if (entity.getDoctorName() != null) {
            existing.setDoctorName(entity.getDoctorName());
        }
        if (entity.getDiagnosis() != null) {
            existing.setDiagnosis(entity.getDiagnosis());
        }
        if (entity.getVisitDate() != null) {
            existing.setVisitDate(entity.getVisitDate());
        }
        if (entity.getPrescription() != null) {
            existing.setPrescription(entity.getPrescription());
        }
        if (entity.getAttachments() != null) {
            existing.setAttachments(entity.getAttachments());
        }
        if (entity.getRemark() != null) {
            existing.setRemark(entity.getRemark());
        }
        existing.setUpdateTime(LocalDateTime.now());
        medicalRecordMapper.updateById(existing);
    }

    @Override
    public void adminDelete(Long id) {
        if (medicalRecordMapper.selectById(id) == null) {
            throw new BusinessException(404, "就诊记录不存在");
        }
        medicalRecordMapper.deleteById(id);
    }

    /** 校验当前家属是否已绑定该老人（status=1） */
    private void assertBound(Long familyUserId, Long elderInfoId) {
        Long count = familyBindingMapper.selectCount(new LambdaQueryWrapper<FamilyBinding>()
                .eq(FamilyBinding::getFamilyUserId, familyUserId)
                .eq(FamilyBinding::getElderInfoId, elderInfoId)
                .eq(FamilyBinding::getStatus, 1));
        if (count == null || count == 0) {
            throw new BusinessException(403, "您尚未绑定该老人，无法添加医疗记录");
        }
    }

    /** 根据 elder_info_id 填充非持久化字段 elderName */
    private void fillElderName(MedicalRecord entity) {
        if (entity.getElderInfoId() != null) {
            ElderInfo elder = elderInfoMapper.selectById(entity.getElderInfoId());
            entity.setElderName(elder != null ? elder.getRealName() : null);
        }
    }
}
