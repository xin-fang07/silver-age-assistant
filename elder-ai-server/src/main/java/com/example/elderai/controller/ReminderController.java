package com.example.elderai.controller;

import com.example.elderai.common.Result;
import com.example.elderai.dto.ReminderDTO;
import com.example.elderai.entity.Reminder;
import com.example.elderai.service.ReminderService;
import com.example.elderai.security.SecurityUtils;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 提醒事项控制器
 * <p>
 * 提供老年人提醒事项的增删改查和完成标记功能。
 * 支持用药提醒、体检提醒、活动提醒等多种类型。
 * 所有操作仅限当前登录用户自己的提醒事项。
 * </p>
 *
 * @author elder-ai-team
 */
@RestController
@RequestMapping("/api/reminder")
public class ReminderController {

    /** 注入提醒事项服务，处理提醒的增删改查和状态变更 */
    @Resource
    private ReminderService reminderService;

    /**
     * 从 HTTP 请求头中提取 JWT Token 并解析出当前登录用户的 ID
     *
     * @return 当前登录用户的 ID
     */
    private Long getCurrentUserId() {
        return SecurityUtils.currentUserId();
    }

    /**
     * 查询当前用户的所有提醒事项
     * <p>
     * 返回该用户创建的提醒列表，按提醒时间升序排列，
     * 即将到来的提醒排在前面，方便用户查看。
     * </p>
     *
     * @return Result 对象，data 为提醒事项列表
     */
    @GetMapping("/list")
    public Result<List<Reminder>> list() {
        Long userId = getCurrentUserId();
        // 调用 reminderService.listByUser() 查询提醒列表
        List<Reminder> list = reminderService.listByUser(userId);
        return Result.success(list);
    }

    /**
     * 新增提醒事项
     * <p>
     * 创建一个新的提醒，初始状态为"待提醒"（status=0）。
     * 需要提供提醒标题、类型和提醒时间，内容可选。
     * </p>
     *
     * @param dto 提醒事项请求参数（包含 title、remindType、remindTime 等）
     * @return Result 对象，data 为创建成功的提醒记录
     */
    @PostMapping("/add")
    public Result<Reminder> add(@Valid @RequestBody ReminderDTO dto) {
        Long userId = getCurrentUserId();
        // 调用 reminderService.add() 创建新提醒
        Reminder reminder = reminderService.add(userId, dto);
        return Result.success("提醒创建成功", reminder);
    }

    /**
     * 修改提醒事项
     * <p>
     * 更新指定提醒的内容、类型或提醒时间。
     * Service 层会验证该提醒属于当前用户，防止越权修改。
     * </p>
     *
     * @param id  提醒 ID
     * @param dto 提醒事项更新参数
     * @return Result 对象，data 为更新后的提醒记录
     */
    @PutMapping("/{id}")
    public Result<Reminder> update(@PathVariable Long id,
                                   @Valid @RequestBody ReminderDTO dto) {
        Long userId = getCurrentUserId();
        // 调用 reminderService.update() 修改提醒
        Reminder reminder = reminderService.update(id, userId, dto);
        return Result.success("提醒修改成功", reminder);
    }

    /**
     * 删除提醒事项
     * <p>
     * 物理删除指定提醒记录。Service 层会校验归属权。
     * 此操作不可逆，建议前端弹出确认提示。
     * </p>
     *
     * @param id 提醒 ID
     * @return Result 对象，操作结果提示
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        // 调用 reminderService.delete() 删除提醒
        reminderService.delete(id, userId);
        return Result.success();
    }

    /**
     * 标记提醒为已完成
     * <p>
     * 将提醒状态从"待提醒"（status=0）更新为"已完成"（status=1）。
     * 已完成和已过期的提醒不可再次标记完成。
     * </p>
     *
     * @param id 提醒 ID
     * @return Result 对象，操作结果提示
     */
    @PutMapping("/{id}/complete")
    public Result<Void> complete(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        // 调用 reminderService.complete() 标记完成
        reminderService.complete(id, userId);
        return Result.success();
    }

    @PutMapping("/{id}/snooze")
    public Result<Void> snooze(@PathVariable Long id,
                               @RequestBody(required = false) Map<String, Integer> body) {
        int minutes = body == null || body.get("minutes") == null ? 10 : body.get("minutes");
        reminderService.snooze(id, getCurrentUserId(), minutes);
        return Result.success("已稍后提醒");
    }

    @PutMapping("/{id}/skip")
    public Result<Void> skip(@PathVariable Long id) {
        reminderService.skip(id, getCurrentUserId());
        return Result.success("已跳过本次提醒");
    }

    @GetMapping("/statistics")
    public Result<Map<String, Object>> statistics(
            @RequestParam(required = false, defaultValue = "30") Integer days) {
        return Result.success(reminderService.statistics(getCurrentUserId(), days));
    }

    /**
     * 取消提醒的已完成状态
     * <p>
     * 将提醒状态从"已完成"（status=1）恢复为"待提醒"（status=0）。
     * 未完成的提醒和已过期的提醒不可取消完成。
     * </p>
     *
     * @param id 提醒 ID
     * @return Result 对象，操作结果提示
     */
    @PutMapping("/{id}/uncomplete")
    public Result<Void> uncomplete(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        // 调用 reminderService.uncomplete() 取消完成
        reminderService.uncomplete(id, userId);
        return Result.success();
    }
}
