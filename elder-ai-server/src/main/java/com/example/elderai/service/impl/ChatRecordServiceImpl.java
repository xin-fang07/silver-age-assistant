package com.example.elderai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.elderai.common.BusinessException;
import com.example.elderai.common.PageResult;
import com.example.elderai.dto.PageQueryDTO;
import com.example.elderai.entity.ChatRecord;
import com.example.elderai.mapper.ChatRecordMapper;
import com.example.elderai.service.ChatRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 聊天记录服务实现类
 * <p>
 * 提供聊天记录的查询、关键词搜索、删除和清空功能。
 * 所有操作都会校验记录归属（userId），防止越权访问其他用户的聊天数据。
 * </p>
 *
 * @author elder-ai-team
 */
@Service // 标记为 Spring Service Bean
public class ChatRecordServiceImpl implements ChatRecordService {

    /** 聊天记录数据访问层 */
    @Autowired
    private ChatRecordMapper chatRecordMapper;

    // ==================== 分页查询 ====================

    /**
     * 分页查询当前用户的聊天记录
     * <p>
     * 使用 MyBatis Plus 的 Page 对象实现物理分页，
     * 按创建时间倒序（最新对话在最前面）。
     * </p>
     *
     * @param userId 当前登录用户ID
     * @param dto    分页查询参数（pageNum、pageSize、keyword）
     * @return 分页结果，包含记录列表和分页信息
     */
    @Override
    public PageResult<ChatRecord> listByUser(Long userId, PageQueryDTO dto) {
        // 1. 构建分页对象（MyBatis Plus 分页，页码从1开始）
        Page<ChatRecord> page = new Page<>(dto.getPageNum(), dto.getPageSize());

        // 2. 构建查询条件：限定当前用户，按创建时间倒序
        LambdaQueryWrapper<ChatRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatRecord::getUserId, userId);

        // 如果传入了关键字，对 question 字段进行模糊搜索
        if (dto.getKeyword() != null && !dto.getKeyword().trim().isEmpty()) {
            wrapper.like(ChatRecord::getQuestion, dto.getKeyword().trim());
        }

        // 按创建时间倒序排列
        wrapper.orderByDesc(ChatRecord::getCreateTime);

        // 3. 执行分页查询
        Page<ChatRecord> resultPage = chatRecordMapper.selectPage(page, wrapper);

        // 4. 构建分页返回结果
        return PageResult.pageSuccess(
                resultPage.getRecords(),      // 当前页数据列表
                resultPage.getTotal(),        // 总记录数
                resultPage.getCurrent(),      // 当前页码
                resultPage.getSize()          // 每页大小
        );
    }

    // ==================== 关键词搜索 ====================

    /**
     * 按关键词搜索当前用户的聊天记录
     * <p>
     * 对 question 字段进行 LIKE 模糊匹配，返回所有包含关键词的记录。
     * 注意：此方法不分页，适用于快速搜索场景。
     * </p>
     *
     * @param userId  当前登录用户ID
     * @param keyword 搜索关键词
     * @return 匹配的聊天记录列表
     */
    @Override
    public List<ChatRecord> searchByKeyword(Long userId, String keyword) {
        // 构建查询条件：限定用户 + question 模糊匹配
        LambdaQueryWrapper<ChatRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatRecord::getUserId, userId);
        // LIKE 查询：question 字段包含 keyword
        wrapper.like(ChatRecord::getQuestion, keyword);
        // 按创建时间倒序
        wrapper.orderByDesc(ChatRecord::getCreateTime);

        return chatRecordMapper.selectList(wrapper);
    }

    // ==================== 删除单条 ====================

    /**
     * 删除单条聊天记录
     * <p>
     * 删除前先查询确认该记录存在且属于当前用户，
     * 防止用户通过伪造 ID 删除他人记录。
     * </p>
     *
     * @param id     聊天记录ID
     * @param userId 当前登录用户ID
     * @throws BusinessException 记录不存在或不属于当前用户时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id, Long userId) {
        // 1. 查询记录确认存在
        ChatRecord record = chatRecordMapper.selectById(id);
        if (record == null) {
            throw new BusinessException(404, "聊天记录不存在");
        }

        // 2. 验证记录归属：只能删除自己的聊天记录
        if (!record.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权删除此聊天记录");
        }

        // 3. 执行物理删除
        chatRecordMapper.deleteById(id);
    }

    // ==================== 清空全部 ====================

    /**
     * 清空当前用户的所有聊天记录
     * <p>
     * 删除该用户在 chat_record 表中的全部记录。
     * </p>
     *
     * @param userId 当前登录用户ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clearAll(Long userId) {
        // 构建删除条件：删除 userId 匹配的所有记录
        QueryWrapper<ChatRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        chatRecordMapper.delete(wrapper);
    }
}
