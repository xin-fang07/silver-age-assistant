package com.example.elderai.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 对话请求DTO
 * 接收用户向AI助手发送的问题
 */
@Data
public class ChatRequestDTO {

    /** 用户提问内容，不能为空 */
    @NotBlank(message = "问题内容不能为空")
    @Size(max = 2000, message = "问题内容不能超过2000字")
    private String question;

    /**
     * 用户所在城市（可选），用于天气查询等场景。
     * 如果提供城市名，AI 天气相关回答会优先以该城市为准。
     */
    private String city;

    /**
     * 会话ID（可选），用于多轮对话上下文记忆。
     * 传入相同的 conversationId 会自动携带最近历史对话。
     * 不传或为 null 时视为开启新会话。
     */
    private Long conversationId;
}
