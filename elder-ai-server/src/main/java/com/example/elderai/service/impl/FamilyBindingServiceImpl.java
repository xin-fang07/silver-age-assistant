package com.example.elderai.service.impl;

import com.example.elderai.entity.Device;
import com.example.elderai.entity.HealthRecord;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.elderai.common.BusinessException;
import com.example.elderai.entity.ElderInfo;
import com.example.elderai.entity.FamilyBinding;
import com.example.elderai.entity.User;
import com.example.elderai.mapper.ElderInfoMapper;
import com.example.elderai.mapper.FamilyBindingMapper;
import com.example.elderai.mapper.NotificationMapper;
import com.example.elderai.entity.Notification;
import com.example.elderai.mapper.UserMapper;
import com.example.elderai.mapper.HealthRecordMapper;
import com.example.elderai.mapper.DeviceMapper;
import com.example.elderai.service.FamilyBindingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;

@Service
public class FamilyBindingServiceImpl implements FamilyBindingService {

    @Autowired
    private FamilyBindingMapper familyBindingMapper;
    @Resource
    private NotificationMapper notificationMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private ElderInfoMapper elderInfoMapper;
    @Autowired
    private HealthRecordMapper healthRecordMapper;
    @Autowired
    private DeviceMapper deviceMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bind(Long familyUserId, Long elderInfoId, String relation, String applicationNote) {
        if (elderInfoId == null) {
            throw new BusinessException(400, "请选择要绑定的老人档案");
        }
        ElderInfo elder = elderInfoMapper.selectById(elderInfoId);
        if (elder == null) {
            throw new BusinessException(404, "未找到该老人档案");
        }
        FamilyBinding existing = familyBindingMapper.selectOne(new QueryWrapper<FamilyBinding>()
                .eq("family_user_id", familyUserId).eq("elder_info_id", elderInfoId));
        if (existing != null) {
            if (existing.getStatus() != null && existing.getStatus() == 1) {
                throw new BusinessException(400, "您已经绑定该老人");
            }
            // 已有待审批/已拒绝记录，重新提交直接生效
            existing.setStatus(1);
            existing.setRelation(relation == null || relation.isBlank() ? "子女" : relation);
            existing.setApplicationNote(applicationNote == null || applicationNote.isBlank() ? null : applicationNote.trim());
            existing.setUpdateTime(LocalDateTime.now());
            familyBindingMapper.updateById(existing);
            return;
        }
        // 家属发起绑定后直接生效，无需审批
        FamilyBinding b = new FamilyBinding();
        b.setFamilyUserId(familyUserId);
        b.setElderInfoId(elderInfoId);
        b.setRelation(relation == null || relation.isBlank() ? "子女" : relation);
        b.setApplicationNote(applicationNote == null || applicationNote.isBlank() ? null : applicationNote.trim());
        b.setStatus(1);
        b.setCreateTime(LocalDateTime.now());
        b.setUpdateTime(LocalDateTime.now());
        familyBindingMapper.insert(b);
    }

    @Override
    public List<Map<String, Object>> myElders(Long familyUserId) {
        List<FamilyBinding> list = familyBindingMapper.selectList(new QueryWrapper<FamilyBinding>()
                .eq("family_user_id", familyUserId).eq("status", 1));
        List<Map<String, Object>> res = new ArrayList<>();
        for (FamilyBinding b : list) {
            Map<String, Object> m = new HashMap<>();
            ElderInfo info = elderInfoMapper.selectById(b.getElderInfoId());
            m.put("elderInfoId", b.getElderInfoId());
            m.put("realName", info != null && info.getRealName() != null
                    ? info.getRealName() : (info != null && info.getNickname() != null ? info.getNickname() : "老人"));
            m.put("username", info != null && info.getRealName() != null ? info.getRealName() : "老人");
            m.put("relation", b.getRelation());
            m.put("phone", info != null ? info.getEmergencyPhone() : null);
            res.add(m);
        }
        return res;
    }

    @Override
    public List<Map<String, Object>> myFamilies(Long elderInfoId) {
        List<FamilyBinding> list = familyBindingMapper.selectList(new QueryWrapper<FamilyBinding>()
                .eq("elder_info_id", elderInfoId).eq("status", 1));
        List<Map<String, Object>> res = new ArrayList<>();
        for (FamilyBinding b : list) {
            Map<String, Object> m = new HashMap<>();
            User fam = userMapper.selectById(b.getFamilyUserId());
            m.put("familyUserId", b.getFamilyUserId());
            m.put("elderInfoId", b.getElderInfoId());
            m.put("username", fam != null ? fam.getUsername() : "");
            m.put("relation", b.getRelation());
            m.put("phone", fam != null ? fam.getPhone() : null);
            res.add(m);
        }
        return res;
    }

    @Override
    public List<Map<String, Object>> availableElders(Long familyUserId) {
        // 当前家属已绑定的老人档案ID集合
        List<FamilyBinding> bound = familyBindingMapper.selectList(new QueryWrapper<FamilyBinding>()
                .eq("family_user_id", familyUserId).in("status", 1, 2, 3));
        List<Long> boundIds = bound.stream().map(FamilyBinding::getElderInfoId).toList();

        QueryWrapper<ElderInfo> qw = new QueryWrapper<>();
        qw.eq("status", 1).orderByDesc("update_time");
        if (!boundIds.isEmpty()) {
            qw.notIn("id", boundIds);
        }
        List<ElderInfo> all = elderInfoMapper.selectList(qw);
        List<Map<String, Object>> res = new ArrayList<>();
        for (ElderInfo e : all) {
            Map<String, Object> m = new HashMap<>();
            m.put("elderInfoId", e.getId());
            m.put("realName", e.getRealName() != null ? e.getRealName()
                    : (e.getNickname() != null ? e.getNickname() : "未命名老人"));
            m.put("nickname", e.getNickname());
            m.put("gender", e.getGender());
            m.put("age", e.getAge());
            m.put("healthStatus", e.getHealthStatus());
            m.put("avatar", e.getAvatar());
            res.add(m);
        }
        return res;
    }

    @Override
    public List<Map<String, Object>> myRequests(Long familyUserId) {
        List<FamilyBinding> list = familyBindingMapper.selectList(new QueryWrapper<FamilyBinding>()
                .eq("family_user_id", familyUserId).in("status", 2, 3).orderByDesc("update_time"));
        List<Map<String, Object>> res = new ArrayList<>();
        for (FamilyBinding b : list) {
            ElderInfo info = elderInfoMapper.selectById(b.getElderInfoId());
            Map<String, Object> m = new HashMap<>();
            m.put("bindingId", b.getId());
            m.put("elderInfoId", b.getElderInfoId());
            m.put("username", info != null && info.getRealName() != null ? info.getRealName() : "未知老人");
            m.put("relation", b.getRelation());
            m.put("status", b.getStatus());
            m.put("updateTime", b.getUpdateTime());
            res.add(m);
        }
        return res;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unbind(Long currentUserId, String role, Long elderInfoId, Long familyUserId) {
        QueryWrapper<FamilyBinding> qw = new QueryWrapper<>();
        if ("FAMILY".equals(role)) {
            qw.eq("family_user_id", currentUserId);
            if (elderInfoId != null) qw.eq("elder_info_id", elderInfoId);
        } else {
            qw.eq("elder_info_id", currentUserId);
            if (familyUserId != null) qw.eq("family_user_id", familyUserId);
        }
        List<FamilyBinding> list = familyBindingMapper.selectList(qw);
        for (FamilyBinding b : list) {
            b.setStatus(0);
            familyBindingMapper.updateById(b);
            // 老人为档案（无独立账号），解绑通知发给家属用户
            Long recipient = b.getFamilyUserId();
            Notification n = new Notification();
            n.setUserId(recipient);
            n.setType("BINDING");
            n.setTitle("照护绑定已解除");
            n.setContent("对方已解除照护绑定，健康、提醒、预警和求助信息将不再共享。");
            n.setRefId(b.getId());
            n.setIsRead(0);
            n.setCreateTime(LocalDateTime.now());
            notificationMapper.insert(n);
        }
    }

    @Override
    public void assertBound(Long familyUserId, Long elderInfoId) {
        FamilyBinding b = familyBindingMapper.selectOne(new QueryWrapper<FamilyBinding>()
                .eq("family_user_id", familyUserId).eq("elder_info_id", elderInfoId).eq("status", 1));
        if (b == null) {
            throw new BusinessException(403, "您尚未绑定该老人，无法查看其数据");
        }
    }
    @Override
    public Map<String, Object> detail(Long familyUserId, Long elderInfoId) {
        // 仅允许查看已绑定(status=1)的老人，不改写任何绑定逻辑
        assertBound(familyUserId, elderInfoId);
        ElderInfo info = elderInfoMapper.selectById(elderInfoId);
        FamilyBinding binding = familyBindingMapper.selectOne(new QueryWrapper<FamilyBinding>()
                .eq("family_user_id", familyUserId).eq("elder_info_id", elderInfoId).eq("status", 1));
        Map<String, Object> res = new HashMap<>();
        res.put("elderInfoId", info != null ? info.getId() : elderInfoId);
        res.put("realName", info != null ? info.getRealName() : null);
        res.put("nickname", info != null ? info.getNickname() : null);
        res.put("gender", info != null ? info.getGender() : null);
        res.put("age", info != null ? info.getAge() : null);
        res.put("birthDate", info != null ? info.getBirthDate() : null);
        res.put("idCard", info != null ? info.getIdCard() : null);
        res.put("bloodType", info != null ? info.getBloodType() : null);
        res.put("emergencyContact", info != null ? info.getEmergencyContact() : null);
        res.put("emergencyPhone", info != null ? info.getEmergencyPhone() : null);
        res.put("address", info != null ? info.getAddress() : null);
        res.put("height", info != null ? info.getHeight() : null);
        res.put("weight", info != null ? info.getWeight() : null);
        res.put("medicalHistory", info != null ? info.getMedicalHistory() : null);
        res.put("healthStatus", info != null ? info.getHealthStatus() : null);
        res.put("relation", binding != null ? binding.getRelation() : null);
        HealthRecord latest = healthRecordMapper.selectOne(new QueryWrapper<HealthRecord>()
                .eq("elder_info_id", elderInfoId).orderByDesc("measured_at").last("limit 1"));
        res.put("healthRecord", latest);
        List<Device> devices = deviceMapper.selectList(new QueryWrapper<Device>().eq("elder_id", elderInfoId));
        res.put("devices", devices);
        return res;
    }

}