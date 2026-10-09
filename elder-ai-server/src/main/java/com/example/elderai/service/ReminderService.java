package com.example.elderai.service;

import com.example.elderai.dto.ReminderDTO;
import com.example.elderai.entity.Reminder;

import java.util.List;
import java.util.Map;

/**
 * 提醒事项服务接口
 * <p>
 * 提供提醒事项的增删改查、完成标记和定时过期检查功能。
 * 所有操作均需校验用户身份。
 * </p>
 *
 * @author elder-ai-team
 */
public interface ReminderService {

    /**
     * 查询当前用户的所有提醒
     * <p>
     * 返回该用户创建的提醒列表，按提醒时间升序。
     * </p>
     *
     * @param userId 当前登录用户ID
     * @return 提醒事项列表
     */
    List<Reminder> listByUser(Long userId);

    /**
     * 新增提醒事项
     * <p>
     * 创建一个新的提醒，初始状态为"待提醒"（status=0）。
     * </p>
     *
     * @param userId 当前登录用户ID
     * @param dto    提醒事项请求参数
     * @return 创建成功的提醒记录
     */
    Reminder add(Long userId, ReminderDTO dto);

    /**
     * 修改提醒事项
     * <p>
     * 需验证该提醒属于当前用户，防止越权修改。
     * </p>
     *
     * @param id     提醒ID
     * @param userId 当前登录用户ID
     * @param dto    提醒事项请求参数
     * @return 更新后的提醒记录
     */
    Reminder update(Long id, Long userId, ReminderDTO dto);

    /**
     * 删除提醒事项
     * <p>
     * 需验证该提醒属于当前用户。
     * </p>
     *
     * @param id     提醒ID
     * @param userId 当前登录用户ID
     */
    void delete(Long id, Long userId);

    /** 暂停或恢复提醒。暂停状态为3，恢复后为待提醒状态0。 */
    void setPaused(Long id, Long userId, boolean paused);

    /**
     * 标记提醒为已完成
     * <p>
     * 将提醒状态更新为"已完成"（status=1）。
     * 需验证该提醒属于当前用户。
     * </p>
     *
     * @param id     提醒ID
     * @param userId 当前登录用户ID
     */
    void complete(Long id, Long userId);

    /** 到点后稍后提醒。 */
    void snooze(Long id, Long userId, int minutes);

    /** 主动跳过本次提醒。 */
    void skip(Long id, Long userId);

    /** 指定用户的提醒执行统计。 */
    Map<String, Object> statistics(Long userId, int days);

    /**
     * 取消提醒的已完成状态
     * <p>
     * 将提醒状态从"已完成"（status=1）恢复为"待提醒"（status=0）。
     * 需验证该提醒属于当前用户。
     * </p>
     *
     * @param id     提醒ID
     * @param userId 当前登录用户ID
     */
    void uncomplete(Long id, Long userId);

    /**
     * 定时任务：检查并标记过期提醒
     * <p>
     * 每分钟执行一次（@Scheduled），查询所有状态为"待提醒"（status=0）
     * 且提醒时间已过的记录，将其状态更新为"已过期"（status=2）。
     * 弹窗提醒由前端轮询实现，本方法只做数据库标记。
     * </p>
     */
    /**
     * 模拟将提醒推送到老人设备（虚拟推送，不接真实硬件）：
     * 将 push_status 置为 PUSHED，并记录推送目标设备号与推送时间。
     *
     * @param reminderId 提醒ID
     */
    void simulatePush(Long reminderId);

    /**
     * 模拟老人确认收到提醒（老人不在系统内操作，由设备端模拟确认）：
     * 将 confirm_status 置为 CONFIRMED，并写入确认流水。
     *
     * @param reminderId 提醒ID
     */
    void confirmByElder(Long reminderId);

    void checkAndNotify();
}
