package com.example.elderai.service;

import com.example.elderai.common.PageResult;
import com.example.elderai.dto.PageQueryDTO;
import com.example.elderai.entity.ChatRecord;

import java.util.List;

/**
 * 聊天记录服务接口
 * <p>
 * 提供用户聊天记录的查询、搜索、删除等操作。
 * 所有操作均需校验用户身份，确保只能操作自己的记录。
 * </p>
 *
 * @author elder-ai-team
 */
public interface ChatRecordService {

    /**
     * 分页查询当前用户的聊天记录
     * <p>
     * 按创建时间倒序排列，最新的对话排在最前面。
     * </p>
     *
     * @param userId 当前登录用户ID
     * @param dto    分页查询参数（包含 pageNum、pageSize、keyword）
     * @return 分页结果，包含聊天记录列表和分页信息
     */
    PageResult<ChatRecord> listByUser(Long userId, PageQueryDTO dto);

    /**
     * 按关键词搜索当前用户的聊天记录
     * <p>
     * 对 question 字段进行模糊匹配（LIKE 查询）。
     * </p>
     *
     * @param userId  当前登录用户ID
     * @param keyword 搜索关键词
     * @return 匹配的聊天记录列表
     */
    List<ChatRecord> searchByKeyword(Long userId, String keyword);

    /**
     * 删除单条聊天记录
     * <p>
     * 需验证该记录属于当前用户，防止越权删除。
     * </p>
     *
     * @param id     聊天记录ID
     * @param userId 当前登录用户ID
     */
    void deleteById(Long id, Long userId);

    /**
     * 清空当前用户的所有聊天记录
     * <p>
     * 删除该用户在 chat_record 表中的全部记录。
     * </p>
     *
     * @param userId 当前登录用户ID
     */
    void clearAll(Long userId);
}
