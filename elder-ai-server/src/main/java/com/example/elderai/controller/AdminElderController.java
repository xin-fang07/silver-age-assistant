package com.example.elderai.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.elderai.common.BusinessException;
import com.example.elderai.common.Result;
import com.example.elderai.entity.Device;
import com.example.elderai.entity.ElderInfo;
import com.example.elderai.entity.FamilyBinding;
import com.example.elderai.entity.User;
import com.example.elderai.mapper.DeviceMapper;
import com.example.elderai.mapper.ElderInfoMapper;
import com.example.elderai.mapper.FamilyBindingMapper;
import com.example.elderai.mapper.UserMapper;
import jakarta.annotation.Resource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/elders")
public class AdminElderController {

    @Resource
    private ElderInfoMapper elderInfoMapper;
    @Resource
    private FamilyBindingMapper familyBindingMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private DeviceMapper deviceMapper;

    @GetMapping
    public Result<List<Map<String, Object>>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        QueryWrapper<ElderInfo> qw = new QueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            qw.and(w -> w.like("real_name", keyword).or().like("nickname", keyword)
                    .or().like("emergency_phone", keyword));
        }
        if (status != null) {
            qw.eq("status", status);
        }
        qw.orderByDesc("update_time");
        List<ElderInfo> elders = elderInfoMapper.selectList(qw);
        List<Map<String, Object>> res = new ArrayList<>();
        for (ElderInfo e : elders) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", e.getId());
            m.put("realName", e.getRealName());
            m.put("nickname", e.getNickname());
            m.put("gender", e.getGender());
            m.put("age", e.getAge());
            m.put("birthDate", e.getBirthDate());
            m.put("phone", e.getEmergencyPhone());
            m.put("address", e.getAddress());
            m.put("healthStatus", e.getHealthStatus());
            m.put("status", e.getStatus());
            m.put("updateTime", e.getUpdateTime());
            long bound = familyBindingMapper.selectCount(new QueryWrapper<FamilyBinding>()
                    .eq("elder_info_id", e.getId()).eq("status", 1));
            m.put("boundFamilyCount", bound);
            long dev = deviceMapper.selectCount(new QueryWrapper<Device>().eq("elder_id", e.getId()));
            m.put("deviceCount", dev);
            res.add(m);
        }
        return Result.success(res);
    }

    @GetMapping("/{id}")
    public Result<Map<String, Object>> detail(@PathVariable Long id) {
        ElderInfo e = elderInfoMapper.selectById(id);
        if (e == null) {
            throw new BusinessException(404, "not found");
        }
        Map<String, Object> res = new HashMap<>();
        res.put("id", e.getId());
        res.put("realName", e.getRealName());
        res.put("nickname", e.getNickname());
        res.put("gender", e.getGender());
        res.put("age", e.getAge());
        res.put("birthDate", e.getBirthDate());
        res.put("idCard", e.getIdCard());
        res.put("bloodType", e.getBloodType());
        res.put("emergencyContact", e.getEmergencyContact());
        res.put("emergencyPhone", e.getEmergencyPhone());
        res.put("phone", e.getEmergencyPhone());
        res.put("address", e.getAddress());
        res.put("height", e.getHeight());
        res.put("weight", e.getWeight());
        res.put("medicalHistory", e.getMedicalHistory());
        res.put("healthStatus", e.getHealthStatus());
        res.put("avatar", e.getAvatar());
        res.put("status", e.getStatus());
        List<FamilyBinding> bindings = familyBindingMapper.selectList(new QueryWrapper<FamilyBinding>()
                .eq("elder_info_id", id).eq("status", 1));
        List<Map<String, Object>> fam = new ArrayList<>();
        for (FamilyBinding b : bindings) {
            Map<String, Object> fm = new HashMap<>();
            User u = userMapper.selectById(b.getFamilyUserId());
            fm.put("bindingId", b.getId());
            fm.put("familyUserId", b.getFamilyUserId());
            fm.put("familyUsername", u != null ? u.getUsername() : "");
            fm.put("familyRealName", u != null ? u.getNickname() : null);
            fm.put("relation", b.getRelation());
            fm.put("status", b.getStatus());
            fam.add(fm);
        }
        res.put("families", fam);
        List<Device> devices = deviceMapper.selectList(new QueryWrapper<Device>().eq("elder_id", id));
        res.put("devices", devices);
        return Result.success(res);
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody ElderInfo body) {
        ElderInfo e = elderInfoMapper.selectById(id);
        if (e == null) {
            throw new BusinessException(404, "not found");
        }
        if (body.getRealName() != null) e.setRealName(body.getRealName());
        if (body.getNickname() != null) e.setNickname(body.getNickname());
        if (body.getGender() != null) e.setGender(body.getGender());
        if (body.getAge() != null) e.setAge(body.getAge());
        if (body.getBirthDate() != null) e.setBirthDate(body.getBirthDate());
        if (body.getIdCard() != null) e.setIdCard(body.getIdCard());
        if (body.getBloodType() != null) e.setBloodType(body.getBloodType());
        if (body.getEmergencyContact() != null) e.setEmergencyContact(body.getEmergencyContact());
        if (body.getEmergencyPhone() != null) e.setEmergencyPhone(body.getEmergencyPhone());
        if (body.getAddress() != null) e.setAddress(body.getAddress());
        if (body.getHeight() != null) e.setHeight(body.getHeight());
        if (body.getWeight() != null) e.setWeight(body.getWeight());
        if (body.getMedicalHistory() != null) e.setMedicalHistory(body.getMedicalHistory());
        if (body.getHealthStatus() != null) e.setHealthStatus(body.getHealthStatus());
        if (body.getAvatar() != null) e.setAvatar(body.getAvatar());
        if (body.getStatus() != null) e.setStatus(body.getStatus());
        e.setUpdateTime(LocalDateTime.now());
        elderInfoMapper.updateById(e);
        return Result.success("updated");
    }

    @DeleteMapping("/{id}")
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> remove(@PathVariable Long id) {
        ElderInfo e = elderInfoMapper.selectById(id);
        if (e == null) {
            throw new BusinessException(404, "not found");
        }
        List<Device> devices = deviceMapper.selectList(new QueryWrapper<Device>().eq("elder_id", id));
        for (Device d : devices) {
            d.setElderId(null);
            d.setUpdateTime(LocalDateTime.now());
            deviceMapper.updateById(d);
        }
        List<FamilyBinding> bindings = familyBindingMapper.selectList(
                new QueryWrapper<FamilyBinding>().eq("elder_info_id", id).eq("status", 1));
        for (FamilyBinding b : bindings) {
            b.setStatus(0);
            b.setUpdateTime(LocalDateTime.now());
            familyBindingMapper.updateById(b);
        }
        elderInfoMapper.deleteById(id);
        return Result.success("deleted");
    }

    @GetMapping("/{id}/bindings")
    public Result<List<Map<String, Object>>> bindings(@PathVariable Long id) {
        List<FamilyBinding> list = familyBindingMapper.selectList(new QueryWrapper<FamilyBinding>()
                .eq("elder_info_id", id).orderByDesc("status").orderByDesc("update_time"));
        List<Map<String, Object>> res = new ArrayList<>();
        for (FamilyBinding b : list) {
            Map<String, Object> m = new HashMap<>();
            User u = userMapper.selectById(b.getFamilyUserId());
            m.put("bindingId", b.getId());
            m.put("familyUserId", b.getFamilyUserId());
            m.put("familyUsername", u != null ? u.getUsername() : "");
            m.put("familyRealName", u != null ? u.getNickname() : null);
            m.put("relation", b.getRelation());
            m.put("status", b.getStatus());
            m.put("createTime", b.getCreateTime());
            m.put("updateTime", b.getUpdateTime());
            res.add(m);
        }
        return Result.success(res);
    }

    @PostMapping("/bindings")
    public Result<Void> addBinding(@RequestBody Map<String, Object> body) {
        Long elderInfoId = body.get("elderInfoId") != null
                ? Long.valueOf(body.get("elderInfoId").toString()) : null;
        Long familyUserId = body.get("familyUserId") != null
                ? Long.valueOf(body.get("familyUserId").toString()) : null;
        String relation = body.get("relation") != null ? body.get("relation").toString() : "zi-nv";
        if (elderInfoId == null || familyUserId == null) {
            throw new BusinessException(400, "missing");
        }
        ElderInfo elder = elderInfoMapper.selectById(elderInfoId);
        if (elder == null) {
            throw new BusinessException(404, "not found");
        }
        User u = userMapper.selectById(familyUserId);
        if (u == null || !"FAMILY".equals(u.getRole())) {
            throw new BusinessException(400, "invalid family");
        }
        FamilyBinding existing = familyBindingMapper.selectOne(new QueryWrapper<FamilyBinding>()
                .eq("family_user_id", familyUserId).eq("elder_info_id", elderInfoId).eq("status", 1));
        if (existing != null) {
            throw new BusinessException(400, "already bound");
        }
        FamilyBinding b = new FamilyBinding();
        b.setFamilyUserId(familyUserId);
        b.setElderInfoId(elderInfoId);
        b.setRelation(relation);
        b.setStatus(1);
        b.setCreateTime(LocalDateTime.now());
        b.setUpdateTime(LocalDateTime.now());
        familyBindingMapper.insert(b);
        return Result.success("bound");
    }

    @DeleteMapping("/bindings/{bindingId}")
    public Result<Void> removeBinding(@PathVariable Long bindingId) {
        FamilyBinding b = familyBindingMapper.selectById(bindingId);
        if (b == null) {
            throw new BusinessException(404, "not found");
        }
        b.setStatus(0);
        b.setUpdateTime(LocalDateTime.now());
        familyBindingMapper.updateById(b);
        return Result.success("unbound");
    }

    @GetMapping("/family-options")
    public Result<List<Map<String, Object>>> familyOptions() {
        List<User> users = userMapper.selectList(new QueryWrapper<User>()
                .eq("role", "FAMILY").eq("status", 1).orderByDesc("id"));
        List<Map<String, Object>> res = new ArrayList<>();
        for (User u : users) {
            Map<String, Object> m = new HashMap<>();
            m.put("familyUserId", u.getId());
            m.put("username", u.getUsername());
            m.put("realName", u.getNickname());
            m.put("phone", u.getPhone());
            res.add(m);
        }
        return Result.success(res);
    }
}
