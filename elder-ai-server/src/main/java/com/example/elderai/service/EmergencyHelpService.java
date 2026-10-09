package com.example.elderai.service;

import com.example.elderai.common.PageResult;
import com.example.elderai.dto.EmergencyHelpDTO;
import com.example.elderai.dto.PageQueryDTO;
import com.example.elderai.entity.EmergencyHelp;

import java.util.List;

/**
 * 紧急求助服务接口
 * <p>
 * 提供紧急求助的创建、查询和处理功能。
 * 老年人用户可发起求助，管理员可查看并处理求助请求。
 * </p>
 *
 * @author elder-ai-team
 */
public interface EmergencyHelpService {

    /**
     * 创建紧急求助
     * <p>
     * 老年人用户发起紧急求助，初始状态为"待处理"（status=0）。
     * </p>
     *
     * @param userId 当前登录用户ID
     * @param dto    紧急求助请求参数
     * @return 创建成功的求助记录
     */
    EmergencyHelp createHelp(Long userId, EmergencyHelpDTO dto, String idempotencyKey);

    /**
     * 查询当前用户的求助记录
     * <p>
     * 按创建时间倒序排列，最新的求助排在最前面。
     * </p>
     *
     * @param userId 当前登录用户ID
     * @return 求助记录列表
     */
    List<EmergencyHelp> listByUser(Long userId);

    EmergencyHelp getById(Long id);

    /**
     * 管理员查看所有求助记录（分页）
     *
     * @param dto 分页查询参数
     * @return 分页结果，包含求助记录列表和分页信息
     */
    PageResult<EmergencyHelp> listAll(PageQueryDTO dto, Integer status);

    /** 管理员按状态机推进求助。 */
    void transition(Long id, Long operatorId, Integer targetStatus, String remark);

    void acknowledgeByFamily(Long id, Long familyUserId, String familyName);

    /** 老人取消尚未接单的求助。 */
    void cancel(Long id, Long userId);

    /** 定时升级长时间无人接单的求助。 */
    void escalateOverdue();
}
