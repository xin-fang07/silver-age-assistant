package com.example.elderai.service;

import com.example.elderai.dto.ElderInfoDTO;
import com.example.elderai.dto.LoginDTO;
import com.example.elderai.dto.PasswordUpdateDTO;
import com.example.elderai.dto.RegisterDTO;

import java.util.Map;

/**
 * 用户服务接口
 * <p>
 * 提供用户注册、登录、信息查询和密码修改等核心用户功能。
 * 所有涉及密码的操作均使用 BCrypt 进行加密和验证。
 * </p>
 *
 * @author elder-ai-team
 */
public interface UserService {

    /**
     * 用户登录
     * <p>
     * 验证用户名和密码，登录成功后返回 JWT Token 和用户基本信息。
     * 流程：
     * <ol>
     *   <li>根据用户名查询用户</li>
     *   <li>使用 BCryptPasswordEncoder.matches() 验证密码</li>
     *   <li>检查用户状态（被禁用则抛出 BusinessException）</li>
     *   <li>生成 JWT Token</li>
     *   <li>返回包含 Token 和用户信息的 Map</li>
     * </ol>
     * </p>
     *
     * @param dto 包含用户名和密码的登录请求
     * @return Map 包含 "token"（JWT令牌）和 "userInfo"（用户信息）两个键
     * @throws com.example.elderai.common.BusinessException 用户名不存在、密码错误或用户被禁用时抛出
     */
    Map<String, Object> login(LoginDTO dto);

    /**
     * 用户注册
     * <p>
     * 创建新用户账号，同时对密码进行 BCrypt 加密。
     * 注册成功后自动为老年人用户创建一个空的 ElderInfo 记录。
     * 流程：
     * <ol>
     *   <li>检查用户名是否已被占用</li>
     *   <li>BCrypt 加密密码</li>
     *   <li>保存用户到 user 表（角色默认 ELDER，状态默认正常）</li>
     *   <li>创建关联的 ElderInfo 空记录</li>
     * </ol>
     * </p>
     *
     * @param dto 包含用户名、密码和手机号的注册请求
     * @throws com.example.elderai.common.BusinessException 用户名已存在时抛出
     */
    void register(RegisterDTO dto);

    /**
     * 获取用户信息
     * <p>
     * 返回用户基本信息（User 表）以及关联的老年人详细信息（ElderInfo 表）。
     * </p>
     *
     * @param userId 用户ID
     * @return Map 包含 "user"（用户基本信息）和 "elderInfo"（老人详细信息）两个键
     */
    Map<String, Object> getUserInfo(Long userId);

    /**
     * 修改密码
     * <p>
     * 验证旧密码正确后，将密码更新为新密码（BCrypt 加密）。
     * 流程：
     * <ol>
     *   <li>查询用户</li>
     *   <li>BCryptPasswordEncoder.matches() 验证旧密码</li>
     *   <li>BCrypt 加密新密码并更新</li>
     * </ol>
     * </p>
     *
     * @param userId 用户ID
     * @param dto    包含旧密码和新密码的请求
     * @throws com.example.elderai.common.BusinessException 旧密码验证失败时抛出
     */
    void updatePassword(Long userId, PasswordUpdateDTO dto);

    /**
     * 更新老年人详细信息
     * <p>
     * 根据 ElderInfoDTO 中的字段更新 ElderInfo 表记录。
     * 仅更新非 null 的字段，保留原有值不变。
     * </p>
     *
     * @param userId 用户ID
     * @param dto    老年人信息更新参数
     */
    void updateElderInfo(Long userId, ElderInfoDTO dto);

    /**
     * 更新用户基本信息
     * <p>
     * 更新 User 表中的头像、昵称、电话等基本信息。
     * </p>
     *
     * @param userId 用户ID
     * @param data   用户基本信息（包含 avatar、nickname、phone）
     */
    void updateProfile(Long userId, Map<String, Object> data);

    /**
     * 退出登录：将当前 Token 加入黑名单使其立即失效。
     *
     * @param token 当前 JWT（不含 Bearer 前缀）
     */
    void logout(String token);
}
