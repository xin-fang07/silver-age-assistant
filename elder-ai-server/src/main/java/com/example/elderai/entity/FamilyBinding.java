package com.example.elderai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/** 家属-老人绑定关系。 */
@Data
@TableName("family_binding")
public class FamilyBinding {
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 家属用户ID */
    private Long familyUserId;
    /** 老人档案ID（elder_info.id） */
    private Long elderInfoId;
    /** 关系描述：子女 / 配偶 / 其他 */
    private String relation;
    private String applicationNote;
    /** 状态：0-已解除 1-有效 2-待老人确认 3-已拒绝 */
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
