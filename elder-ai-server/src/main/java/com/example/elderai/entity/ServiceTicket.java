package com.example.elderai.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;
@Data @TableName("service_ticket")
public class ServiceTicket {
 @TableId(type=IdType.AUTO) private Long id; private String ticketNo; private Long userId;
 private String type; private String subject; private String content; private String contactName;
 private String contactPhone; private String contactEmail; private String status; private String priority;
 private Long handlerId; private String processRemark; private LocalDateTime resolvedAt;
 private LocalDateTime archivedAt; private LocalDateTime createTime; private LocalDateTime updateTime;
}
