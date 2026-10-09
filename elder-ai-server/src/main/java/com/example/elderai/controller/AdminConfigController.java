package com.example.elderai.controller;

import com.example.elderai.common.Result;
import com.example.elderai.entity.SystemConfig;
import com.example.elderai.mapper.SystemConfigMapper;
import com.example.elderai.security.SecurityUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import org.apache.ibatis.session.SqlSession;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/config")
public class AdminConfigController {

    @Resource
    private SystemConfigMapper configMapper;

    @Resource
    private ObjectMapper objectMapper;

    @Resource
    private SqlSession sqlSession;

    private void checkAdmin() {
        SecurityUtils.currentUser();
    }

    @GetMapping
    public Result<Map<String, Object>> getConfig() {
        checkAdmin();
        Map<String, Object> result = new HashMap<>();

        try {
            result.put("health", loadConfigGroup("health"));
            result.put("push", loadConfigGroup("push"));
            result.put("llm", loadConfigGroup("llm"));
            result.put("system", loadConfigGroup("system"));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("加载配置失败", e);
        }

        return Result.success(result);
    }

    @PostMapping
    public Result<Void> saveConfig(@RequestBody Map<String, Object> config) {
        checkAdmin();

        try {
            saveConfigGroup("health", config.get("health"));
            saveConfigGroup("push", config.get("push"));
            saveConfigGroup("llm", config.get("llm"));
            saveConfigGroup("system", config.get("system"));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("保存配置失败", e);
        }

        return Result.success("配置已保存");
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> loadConfigGroup(String type) throws JsonProcessingException {
        List<SystemConfig> list = configMapper.selectByConfigType(type);
        if (list.isEmpty()) {
            return new HashMap<>();
        }
        SystemConfig config = list.get(0);
        return objectMapper.readValue(config.getConfigValue(), Map.class);
    }

    private void saveConfigGroup(String type, Object value) throws JsonProcessingException {
        if (value == null) return;

        List<SystemConfig> existing = configMapper.selectByConfigType(type);
        String jsonValue = objectMapper.writeValueAsString(value);

        if (!existing.isEmpty()) {
            SystemConfig config = existing.get(0);
            config.setConfigValue(jsonValue);
            config.setUpdateTime(LocalDateTime.now());
            configMapper.updateById(config);
        } else {
            SystemConfig config = new SystemConfig();
            config.setConfigKey(type);
            config.setConfigValue(jsonValue);
            config.setConfigType(type);
            config.setCreateTime(LocalDateTime.now());
            config.setUpdateTime(LocalDateTime.now());
            configMapper.insert(config);
        }
    }

    @GetMapping("/admin-users")
    public Result<Map<String, Object>> listAdminUsers(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Integer status) {
        checkAdmin();

        Map<String, Object> params = buildAdminUserParams(keyword, role, status);
        params.put("offset", (pageNum - 1) * pageSize);
        params.put("limit", pageSize);

        List<Map<String, Object>> users = sqlSession.selectList("listAdminUsers", params);
        Long total = sqlSession.selectOne("countAdminUsers", buildAdminUserParams(keyword, role, status));

        Map<String, Object> result = new HashMap<>();
        result.put("list", users);
        result.put("total", total);

        return Result.success(result);
    }

    private Map<String, Object> buildAdminUserParams(String keyword, String role, Integer status) {
        Map<String, Object> params = new HashMap<>();
        if (keyword != null && !keyword.isBlank()) {
            params.put("keyword", "%" + keyword + "%");
            params.put("keyword2", "%" + keyword + "%");
        }
        params.put("role", role);
        params.put("status", status);
        return params;
    }

    @PostMapping("/admin-users")
    public Result<Void> createAdminUser(@RequestBody Map<String, Object> data) {
        checkAdmin();

        String username = (String) data.get("username");
        String nickname = (String) data.get("nickname");
        String phone = (String) data.get("phone");
        String role = (String) data.get("role");
        String password = (String) data.get("password");

        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            return Result.error(400, "用户名和密码不能为空");
        }

        String hashedPassword = org.springframework.security.crypto.bcrypt.BCrypt.hashpw(password,
                org.springframework.security.crypto.bcrypt.BCrypt.gensalt());

        Map<String, Object> insertParams = new HashMap<>();
        insertParams.put("username", username);
        insertParams.put("nickname", nickname);
        insertParams.put("phone", phone);
        insertParams.put("role", role != null ? role : "ADMIN");
        insertParams.put("password", hashedPassword);
        insertParams.put("status", 1);
        insertParams.put("createTime", LocalDateTime.now());
        insertParams.put("updateTime", LocalDateTime.now());
        sqlSession.insert("insertAdminUser", insertParams);

        return Result.success("管理员已创建");
    }

    @PutMapping("/admin-users/{id}/status")
    public Result<Void> updateAdminStatus(@PathVariable Long id, @RequestBody Map<String, Integer> data) {
        checkAdmin();
        Integer status = data.get("status");
        if (status == null) return Result.error(400, "状态不能为空");

        sqlSession.update("updateAdminStatus", Map.of("id", id, "status", status));
        return Result.success("状态已更新");
    }

    @PutMapping("/admin-users/{id}/password")
    public Result<Void> resetAdminPassword(@PathVariable Long id, @RequestBody Map<String, String> data) {
        checkAdmin();
        String password = data.get("password");
        if (password == null || password.isBlank()) return Result.error(400, "密码不能为空");

        String hashedPassword = org.springframework.security.crypto.bcrypt.BCrypt.hashpw(password,
                org.springframework.security.crypto.bcrypt.BCrypt.gensalt());

        sqlSession.update("resetAdminPassword", Map.of("id", id, "password", hashedPassword));
        return Result.success("密码已重置");
    }

    @PutMapping("/admin-users/{id}/role")
    @SuppressWarnings("unchecked")
    public Result<Void> updateAdminRole(@PathVariable Long id, @RequestBody Map<String, Object> data) {
        checkAdmin();

        String role = (String) data.get("role");
        List<String> permissions = (List<String>) data.get("permissions");
        List<Long> managedElders = (List<Long>) data.get("managedElders");

        Map<String, Object> roleParams = new HashMap<>();
        roleParams.put("id", id);
        roleParams.put("role", role);
        roleParams.put("permissions", permissions != null ? String.join(",", permissions) : null);
        sqlSession.update("updateAdminRole", roleParams);

        if (managedElders != null && !managedElders.isEmpty()) {
            sqlSession.delete("clearManagedElders", Map.of("adminId", id));
            for (Long elderId : managedElders) {
                sqlSession.insert("addManagedElder", Map.of(
                        "adminId", id,
                        "elderInfoId", elderId,
                        "createTime", LocalDateTime.now()
                ));
            }
        }

        return Result.success("权限配置已更新");
    }

    @DeleteMapping("/admin-users/{id}")
    public Result<Void> deleteAdminUser(@PathVariable Long id) {
        checkAdmin();
        sqlSession.delete("deleteAdminUser", Map.of("id", id));
        return Result.success("管理员已删除");
    }
}