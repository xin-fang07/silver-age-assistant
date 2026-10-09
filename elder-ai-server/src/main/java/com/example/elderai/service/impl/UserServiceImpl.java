package com.example.elderai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.elderai.common.BusinessException;
import com.example.elderai.dto.ElderInfoDTO;
import com.example.elderai.dto.LoginDTO;
import com.example.elderai.dto.PasswordUpdateDTO;
import com.example.elderai.dto.RegisterDTO;
import com.example.elderai.entity.ElderInfo;
import com.example.elderai.entity.FamilyBinding;
import com.example.elderai.entity.User;
import com.example.elderai.mapper.ElderInfoMapper;
import com.example.elderai.mapper.FamilyBindingMapper;
import com.example.elderai.mapper.UserMapper;
import com.example.elderai.service.UserService;
import com.example.elderai.service.RedisCacheService;
import com.example.elderai.utils.JwtUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户服务实现类
 * <p>
 * 实现用户注册、登录、信息查询和密码修改等核心业务逻辑。
 * 密码使用 BCrypt 加密存储，登录成功后返回 JWT Token。
 * 注册操作涉及 user 表和 elder_info 表的联合写入，使用事务保证一致性。
 * </p>
 *
 * @author elder-ai-team
 */
@Service // 标记为 Spring Service Bean
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    /** 用户数据访问层 */
    @Autowired
    private UserMapper userMapper;

    /** 老年人信息数据访问层 */
    @Autowired
    private ElderInfoMapper elderInfoMapper;

    /** 家属-老人绑定关系数据访问层（用于按家属解析绑定的老人档案） */
    @Autowired
    private FamilyBindingMapper familyBindingMapper;

    /**
     * 解析当前家属绑定的首位老人档案ID。
     * 架构调整后老人不再作为登录用户，老人档案通过 family_binding 与家属关联；
     * 未绑定任何老人时返回 null。
     */
    private Long resolveElderOfFamily(Long familyUserId) {
        if (familyUserId == null) {
            return null;
        }
        FamilyBinding binding = familyBindingMapper.selectOne(
                new QueryWrapper<FamilyBinding>()
                        .eq("family_user_id", familyUserId).eq("status", 1).last("LIMIT 1"));
        return binding != null ? binding.getElderInfoId() : null;
    }

    /** JWT 工具类，用于生成和解析 Token */
    @Autowired
    private JwtUtils jwtUtils;

    /** BCrypt 密码编码器，用于密码加密和验证 */
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private RedisCacheService redisCacheService;

    // ==================== 登录 ====================

    /**
     * 用户登录
     * <p>
     * 三步验证流程：
     * 1. 根据用户名或手机号查询用户是否存在
     * 2. 使用 BCrypt 验证密码是否匹配
     * 3. 检查账号是否被禁用（status != 1）
     * 全部通过后生成 JWT Token 并返回。
     * </p>
     *
     * @param dto 包含用户名和密码的登录请求
     * @return Map 包含 "token"（JWT令牌）和 "userInfo"（用户信息，不含密码）
     * @throws BusinessException 用户名不存在、密码错误或用户被禁用时抛出
     */
    @Override
    public Map<String, Object> login(LoginDTO dto) {
        // 1. 根据用户名查询用户，查不到时再按手机号查询，最后按邮箱查询（支持账号、手机号或邮箱登录）
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("username", dto.getUsername()));
        if (user == null) {
            user = userMapper.selectOne(new QueryWrapper<User>().eq("phone", dto.getUsername()));
        }
        if (user == null) {
            user = userMapper.selectOne(new QueryWrapper<User>().eq("email", dto.getUsername()));
        }

        // 用户名/手机号不存在
        if (user == null) {
            throw new BusinessException(401, "用户名或密码错误");
        }

        // 2. 验证密码（BCrypt 慢哈希比对）
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException(401, "用户名或密码错误");
        }

        // 3. 检查账号状态（0=禁用, 1=正常）
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BusinessException(403, "账号已被禁用，请联系管理员");
        }

        user.setLastLoginTime(LocalDateTime.now());
        userMapper.updateById(user);

        // 4. 生成 JWT Token，携带 userId、username、role
        String token = jwtUtils.generateToken(user.getId(), user.getUsername(), user.getRole());

        // 5. 构建返回结果（不返回密码字段）
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);

        // 构造用户信息 Map，排除敏感字段 password
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("role", user.getRole());
        userInfo.put("phone", user.getPhone());
        userInfo.put("avatar", user.getAvatar());
        userInfo.put("status", user.getStatus());
        userInfo.put("createTime", user.getCreateTime());
        result.put("userInfo", userInfo);

        return result;
    }

    // ==================== 注册 ====================

    /**
     * 用户注册
     * <p>
     * 注册流程需要同时操作 user 表和 elder_info 表，
     * 使用 @Transactional 保证事务原子性：要么全部成功，要么全部回滚。
     * </p>
     *
     * @param dto 包含用户名、密码和手机号的注册请求
     * @throws BusinessException 用户名已存在时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class) // 事务注解：任何异常都回滚
    public void register(RegisterDTO dto) {
        // 1. 检查用户名是否已被占用
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", dto.getUsername());
        Long count = userMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException(409, "用户名已存在，请换一个用户名");
        }

        // 2. 构建 User 实体并设置字段
        User user = new User();
        user.setUsername(dto.getUsername());
        // BCrypt 加密密码，数据库中不存储明文
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        // 默认角色为老年人用户
        String regRole = dto.getRole();
        // 仅允许家属注册；管理端账号由后台生成。ELDER 角色已废弃（老人改以档案形式管理）。
        if (regRole == null || !"FAMILY".equals(regRole)) {
            regRole = "FAMILY";
        }
        user.setRole(regRole);
        // 默认状态为正常
        user.setStatus(1);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());

        // 3. 插入 user 表（家属注册不再自动创建老人档案；老人以独立档案形式经绑定流程管理）
        userMapper.insert(user);
    }

    // ==================== 获取用户信息 ====================

    /**
     * 获取用户完整信息
     * <p>
     * 同时查询 user 表和 elder_info 表，返回用户基本信息和老年人详细信息。
     * </p>
     *
     * @param userId 用户ID
     * @return Map 包含 "user"（用户信息，不含密码）和 "elderInfo"（老人详细信息）
     * @throws BusinessException 用户不存在时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @SuppressWarnings("unchecked")
    public Map<String, Object> getUserInfo(Long userId) {
        // 缓存：登录用户信息缓存 30 分钟，减少 user + elder_info 连表查询
        String cacheKey = "user:info:" + userId;
        Map<String, Object> cached = redisCacheService.getObject(cacheKey, Map.class);
        if (cached != null) {
            return cached;
        }

        // 1. 查询用户基本信息
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        // 2. 查询当前家属绑定的首位老人档案（架构调整后老人不再作为登录用户，
        //    通过 family_binding 关联；未绑定任何老人时返回 null，前端以空对象兜底）
        Long elderInfoId = resolveElderOfFamily(userId);
        ElderInfo elderInfo = elderInfoId != null ? elderInfoMapper.selectById(elderInfoId) : null;

        // 4. 构建返回结果
        Map<String, Object> result = new HashMap<>();

        // 用户信息（排除密码）
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", user.getId());
        userMap.put("username", user.getUsername());
        userMap.put("role", user.getRole());
        userMap.put("phone", user.getPhone());
        userMap.put("avatar", user.getAvatar());
        userMap.put("status", user.getStatus());
        userMap.put("createTime", user.getCreateTime());
        result.put("user", userMap);
        result.put("elderInfo", elderInfo); // 可能为 null（理论上注册时已创建空记录）

        // 写入缓存（30 分钟）
        redisCacheService.setObject(cacheKey, result, 30);

        return result;
    }

    // ==================== 修改密码 ====================

    /**
     * 修改密码
     * <p>
     * 先验证旧密码是否正确，再对新密码进行 BCrypt 加密后更新。
     * </p>
     *
     * @param userId 用户ID
     * @param dto    包含旧密码和新密码的请求
     * @throws BusinessException 用户不存在或旧密码验证失败时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePassword(Long userId, PasswordUpdateDTO dto) {
        // 1. 查询用户
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        // 2. 验证旧密码是否正确
        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new BusinessException(400, "旧密码不正确");
        }

        // 3. 对新密码进行 BCrypt 加密并更新
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);

        // 密码变更，失效用户信息缓存
        redisCacheService.delete("user:info:" + userId);
    }

    // ==================== 更新老年人信息 ====================

    /**
     * 更新老年人详细信息
     * <p>
     * 根据 DTO 中的非 null 字段选择性更新 ElderInfo 表中的对应字段。
     * 采用 MyBatis Plus 的 updateById 方式，直接覆盖更新。
     * </p>
     *
     * @param userId 用户ID
     * @param dto    老年人信息更新参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateElderInfo(Long userId, ElderInfoDTO dto) {
        // 1. 解析当前家属绑定的首位老人档案（架构调整后老人不再作为登录用户）
        Long elderInfoId = resolveElderOfFamily(userId);
        if (elderInfoId == null) {
            throw new BusinessException(400, "未找到关联的老人档案，请先在「我的老人」中绑定或创建老人");
        }
        ElderInfo elderInfo = elderInfoMapper.selectById(elderInfoId);
        if (elderInfo == null) {
            // 绑定关系存在但档案缺失（数据不一致），补建一条与绑定一致的档案
            elderInfo = new ElderInfo();
            elderInfo.setId(elderInfoId);
            elderInfo.setCreateTime(LocalDateTime.now());
            elderInfoMapper.insert(elderInfo);
            elderInfo = elderInfoMapper.selectById(elderInfoId);
        }

        // 3. 仅更新 DTO 中非 null 的字段（保留原有值不变）
        if (dto.getRealName() != null) {
            elderInfo.setRealName(dto.getRealName());
        }
        if (dto.getGender() != null) {
            elderInfo.setGender(dto.getGender());
        }
        if (dto.getAge() != null) {
            elderInfo.setAge(dto.getAge());
        }
        if (dto.getAddress() != null) {
            elderInfo.setAddress(dto.getAddress());
        }
        if (dto.getEmergencyContact() != null) {
            elderInfo.setEmergencyContact(dto.getEmergencyContact());
        }
        if (dto.getEmergencyPhone() != null) {
            elderInfo.setEmergencyPhone(dto.getEmergencyPhone());
        }
        if (dto.getHeight() != null) {
            elderInfo.setHeight(dto.getHeight());
        }
        if (dto.getNickname() != null) {
            elderInfo.setNickname(dto.getNickname());
        }
        if (dto.getMedicalHistory() != null) {
            elderInfo.setMedicalHistory(dto.getMedicalHistory());
        }

        // 4. 更新修改时间
        elderInfo.setUpdateTime(LocalDateTime.now());

        // 5. 执行保存或更新
        if (elderInfo.getId() == null) {
            elderInfoMapper.insert(elderInfo);
        } else {
            elderInfoMapper.updateById(elderInfo);
        }

        // 资料变更，失效用户信息缓存
        redisCacheService.delete("user:info:" + userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProfile(Long userId, Map<String, Object> data) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        if (data.containsKey("avatar")) {
            user.setAvatar((String) data.get("avatar"));
        }
        if (data.containsKey("nickname")) {
            user.setNickname((String) data.get("nickname"));
        }
        if (data.containsKey("phone")) {
            user.setPhone((String) data.get("phone"));
        }

        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);

        // 资料变更，失效用户信息缓存
        redisCacheService.delete("user:info:" + userId);
    }

    // ==================== 退出登录（Token 黑名单） ====================

    /**
     * 退出登录：将当前 Token 的 jti 写入 Redis 黑名单，TTL = Token 剩余有效期。
     * 黑名单中的 Token 在 JwtAuthenticationFilter 中会被拒绝，实现“主动失效”。
     *
     * @param token 当前请求的 JWT（不含 Bearer 前缀）
     */
    @Override
    public void logout(String token) {
        if (token == null || token.isBlank()) {
            return;
        }
        try {
            String jti = jwtUtils.getId(token);
            Date exp = jwtUtils.getExpiration(token);
            if (jti == null || exp == null) {
                return;
            }
            long ttlSeconds = (exp.getTime() - System.currentTimeMillis()) / 1000;
            if (ttlSeconds <= 0) {
                return;
            }
            redisCacheService.setObjectSeconds("jwt:blacklist:" + jti, "1", ttlSeconds);
        } catch (Exception e) {
            log.warn("登出黑名单写入失败: {}", e.getMessage());
        }
    }
}
