package com.example.elderai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("account_deletion_request")
public class AccountDeletionRequest {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String reason;
    /** PENDING / APPROVED / REJECTED / CANCELLED */
    private String status;
    private LocalDateTime requestedAt;
    private Long processedBy;
    private LocalDateTime processedAt;
    private String processRemark;
}
