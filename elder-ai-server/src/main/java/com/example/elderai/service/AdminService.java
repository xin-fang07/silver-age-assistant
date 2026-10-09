package com.example.elderai.service;

import com.example.elderai.common.PageResult;
import com.example.elderai.dto.PageQueryDTO;
import com.example.elderai.entity.User;

import java.util.Map;

/**
 * 管理员服务接口
 * <p>
 * 提供管理员专属功能：用户管理、控制台统计等。
 * </p>
 *
 * @author elder-ai-team
 */
public interface AdminService {

    /**
     * 分页查询所有用户
     * <p>
     * 管理员可查看系统中所有注册用户（包括老年人和管理员角色）。
     * </p>
     *
     * @param dto 分页查询参数（支持 keyword 搜索用户名）
     * @return 分页结果，包含用户列表和分页信息
     */
    PageResult<User> listUsers(PageQueryDTO dto);

    /**
     * 禁用/启用用户
     * <p>
     * 管理员可以切换用户的状态（0-禁用, 1-正常）。
     * 被禁用的用户将无法登录系统。
     * </p>
     *
     * @param userId 用户ID
     * @param status 目标状态：0-禁用, 1-正常
     */
    void updateUserStatus(Long userId, Integer status);

    /**
     * 获取控制台统计数据
     * <p>
     * 返回管理后台首页需要的关键统计数据，包括：
     * <ul>
     *   <li>userCount - 系统总用户数</li>
     *   <li>todayChatCount - 今日问答次数</li>
     *   <li>pendingEmergencyCount - 待处理求助数</li>
     *   <li>weekReminderCount - 本周提醒数</li>
     * </ul>
     * </p>
     *
     * @return 统计数据 Map
     */
    Map<String, Object> getDashboardStats();
}
