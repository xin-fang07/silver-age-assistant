package com.example.elderai.controller;

import com.example.elderai.common.PageResult;
import com.example.elderai.common.Result;
import com.example.elderai.dto.PageQueryDTO;
import com.example.elderai.entity.ChatRecord;
import com.example.elderai.service.ChatRecordService;
import com.example.elderai.security.SecurityUtils;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;

/**
 * 聊天记录控制器
 * <p>
 * 提供用户聊天记录的查询、搜索、删除和清空功能。
 * 所有操作仅限当前登录用户自己的聊天记录，确保数据隔离。
 * </p>
 *
 * @author elder-ai-team
 */
@RestController
@RequestMapping("/api/chat-record")
public class ChatRecordController {

    /** 注入聊天记录服务，处理记录查询和删除的业务逻辑 */
    @Resource
    private ChatRecordService chatRecordService;

    /**
     * 从 HTTP 请求头中提取 JWT Token 并解析出当前登录用户的 ID
     *
     * @return 当前登录用户的 ID
     */
    private Long getCurrentUserId() {
        return SecurityUtils.currentUserId();
    }

    /**
     * 分页查询当前用户的聊天记录
     * <p>
     * 按创建时间倒序排列，最新的对话排在最前面。
     * GET 请求通过 @RequestParam 接收分页参数，组装为 PageQueryDTO 调用 Service。
     * </p>
     *
     * @param pageNum  当前页码，默认第 1 页
     * @param pageSize 每页记录数，默认 10 条
     * @param keyword  搜索关键字（可选），对问题内容进行模糊匹配
     * @return PageResult 对象，包含聊天记录列表、总记录数、当前页码和每页大小
     */
    @GetMapping("/list")
    public PageResult<ChatRecord> list(@RequestParam(defaultValue = "1") Integer pageNum,
                                       @RequestParam(defaultValue = "10") Integer pageSize,
                                       @RequestParam(required = false) String keyword) {
        Long userId = getCurrentUserId();
        // 组装分页查询参数
        PageQueryDTO dto = new PageQueryDTO();
        dto.setPageNum(pageNum);
        dto.setPageSize(pageSize);
        dto.setKeyword(keyword);
        // 调用 chatRecordService.listByUser() 分页查询
        return chatRecordService.listByUser(userId, dto);
    }

    /**
     * 按关键词搜索当前用户的聊天记录
     * <p>
     * 对问题（question）字段进行模糊匹配，并强制分页，避免历史数据过多时占用大量内存。
     * 适合前端在历史记录中快速查找某次对话。
     * </p>
     *
     * @param keyword 搜索关键词
     * @return 分页后的匹配记录
     */
    @GetMapping("/search")
    public PageResult<ChatRecord> search(@RequestParam String keyword,
                                         @RequestParam(defaultValue = "1") Integer pageNum,
                                         @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = getCurrentUserId();
        PageQueryDTO dto = new PageQueryDTO();
        dto.setPageNum(pageNum);
        dto.setPageSize(pageSize);
        dto.setKeyword(keyword);
        return chatRecordService.listByUser(userId, dto);
    }

    /**
     * 删除单条聊天记录
     * <p>
     * 只能删除当前用户自己的记录，Service 层会校验记录归属权，
     * 防止越权删除他人记录。
     * </p>
     *
     * @param id 聊天记录 ID
     * @return Result 对象，操作结果提示
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteById(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        // 调用 chatRecordService.deleteById() 删除指定记录
        chatRecordService.deleteById(id, userId);
        return Result.success();
    }

    /**
     * 清空当前用户的所有聊天记录
     * <p>
     * 一次性删除该用户在 chat_record 表中的全部记录。
     * 此操作不可逆，建议前端弹出确认提示后再调用。
     * </p>
     *
     * @return Result 对象，操作结果提示
     */
    @DeleteMapping("/clear")
    public Result<Void> clearAll() {
        Long userId = getCurrentUserId();
        // 调用 chatRecordService.clearAll() 清空所有记录
        chatRecordService.clearAll(userId);
        return Result.success();
    }
}
