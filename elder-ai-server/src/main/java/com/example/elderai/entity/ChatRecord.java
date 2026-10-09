package com.example.elderai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 对话记录实体类，对应数据库 chat_record 表
 * 记录用户与AI助手的每一次对话，包括是否启动兜底回复
 */
@Data
@TableName("chat_record")
public class ChatRecord {

    /** 主键ID，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联的用户ID */
    private Long userId;

    /** 会话ID，同一会话的多条记录共享一个ID，用于上下文记忆 */
    private Long conversationId;

    /** 用户提问内容 */
    private String question;

    /** AI助手回答内容 */
    private String answer;

    /** 是否为兜底回复：0-正常回复, 1-兜底回复 */
    private Integer isFallback;

    /** 对话创建时间 */
    private LocalDateTime createTime;
}
