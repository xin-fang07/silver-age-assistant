package com.example.elderai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("news_revision")
public class NewsRevision {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long newsId;
    private Long editorId;
    private String snapshot;
    private LocalDateTime createTime;
}
