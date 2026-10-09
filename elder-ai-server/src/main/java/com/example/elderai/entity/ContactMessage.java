package com.example.elderai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("contact_message")
public class ContactMessage {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String name;
    private String phone;
    private String email;
    private String subject;
    private String message;
    /** PENDING / PROCESSING / REPLIED */
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
