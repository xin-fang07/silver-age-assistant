package com.example.elderai.service;

import java.util.Map;

/**
 * AI 对话服务接口
 * <p>
 * 提供用户与 AI 助手的对话功能，集成了 DeepSeek 大模型和 FAQ 降级机制。
 * 每次对话都会保存到 chat_record 表中，便于历史回顾。
 * </p>
 *
 * @author elder-ai-team
 */
public interface ChatService {

    /**
     * AI 对话（支持多轮上下文）
     * <p>
     * 处理用户的提问，返回 AI 助手的回答。处理流程：
     * <ol>
     *   <li>若传入 conversationId，查询该会话最近 N 轮历史作为上下文</li>
     *   <li>首先尝试调用 DeepSeek 获取 AI 回答（带历史上下文）</li>
     *   <li>如果 DeepSeek 调用失败，则调用 FaqFallbackService 作为降级回答</li>
     *   <li>将对话记录保存到 chat_record 表，关联 conversationId</li>
     *   <li>返回包含对话信息和会话ID的 Map</li>
     * </ol>
     * </p>
     *
     * @param userId         当前登录用户ID
     * @param question       用户提问内容
     * @param city           用户所在城市（可选，用于天气等场景）
     * @param conversationId 会话ID（可选，null 表示开启新会话）
     * @return Map 包含以下键：
     *         <ul>
     *           <li>question       - 用户提问内容</li>
     *           <li>answer         - AI 回答内容</li>
     *           <li>isFallback     - 是否降级回答（0-正常, 1-降级）</li>
     *           <li>createTime     - 对话创建时间</li>
     *           <li>conversationId - 会话ID</li>
     *         </ul>
     */
    Map<String, Object> chat(Long userId, String question, String city, Long conversationId);
}
