package com.example.elderai.controller;

import com.example.elderai.common.Result;
import com.example.elderai.dto.ChatRequestDTO;
import com.example.elderai.service.ChatService;
import com.example.elderai.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.Map;

/**
 * AI 对话控制器
 * <p>
 * 处理用户与 AI 智能助手的对话请求。
 * 调用 DeepSeek 大模型获取智能回答，若 AI 调用失败则自动降级到 FAQ 预设回答。
 * 每次对话记录都会保存到数据库，方便用户回顾历史。
 * </p>
 *
 * @author elder-ai-team
 */
@Tag(name = "智能问答", description = "基于 DeepSeek LLM 的 AI 对话接口，支持多轮上下文")
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    /** 注入 AI 对话服务，处理问答逻辑（含 DeepSeek 调用和 FAQ 降级） */
    @Resource
    private ChatService chatService;

    /**
     * 从 HTTP 请求头中提取 JWT Token 并解析出当前登录用户的 ID
     *
     * @return 当前登录用户的 ID
     */
    private Long getCurrentUserId() {
        return SecurityUtils.currentUserId();
    }

    /**
     * 智能问答（支持多轮上下文）
     * <p>
     * 接收用户提问，通过 AI 大模型生成回答。处理流程：
     * <ol>
     *   <li>若传入 conversationId，自动携带最近 5 轮历史对话作为上下文</li>
     *   <li>尝试调用 DeepSeek AI 获取智能回答</li>
     *   <li>若 DeepSeek 调用失败，自动降级到 FAQ 预设回答库</li>
     *   <li>将对话记录保存到 chat_record 表</li>
     *   <li>返回包含问题、回答、是否降级、时间、会话ID的完整结果</li>
     * </ol>
     * </p>
     *
     * @param dto 对话请求（包含 question、city、conversationId）
     * @return Result 对象，data 中包含 question、answer、isFallback、createTime、conversationId
     */
    @Operation(summary = "智能问答", description = "向 AI 助手提问，支持多轮上下文。传入 conversationId 可延续历史对话。")
    @PostMapping("/ask")
    public Result<Map<String, Object>> ask(@Valid @RequestBody ChatRequestDTO dto) {
        Long userId = getCurrentUserId();
        Map<String, Object> result = chatService.chat(
                userId,
                dto.getQuestion(),
                dto.getCity(),
                dto.getConversationId()
        );
        return Result.success(result);
    }
}
