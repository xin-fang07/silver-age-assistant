package com.example.elderai.service;

import com.example.elderai.common.PageResult;
import com.example.elderai.entity.Activity;
import com.example.elderai.entity.ActivitySignup;

/**
 * 娱乐活动服务接口。
 * 家属端聚焦浏览/报名；管理员端负责内容维护与报名管理。
 *
 * @author elder-ai-team
 */
public interface ActivityService {

    // ---------------- 家属端 ----------------
    /** 已发布活动分页列表，可按类型(ONLINE/OFFLINE)与关键词筛选 */
    PageResult<Activity> familyList(Long familyUserId, String type, String keyword, Integer pageNum, Integer pageSize);

    /** 已发布活动详情 */
    Activity familyDetail(Long id);

    /** 家属为老人报名线下活动，返回报名记录 id */
    Long signup(Long activityId, Long familyUserId, Long elderInfoId, String contactPhone, String remark);

    /** 我的报名记录分页 */
    PageResult<ActivitySignup> mySignups(Long familyUserId, Integer pageNum, Integer pageSize);

    /** 取消我的报名 */
    void cancelSignup(Long signupId, Long familyUserId);

    // ---------------- 管理员端 ----------------
    PageResult<Activity> adminList(String type, String keyword, Integer pageNum, Integer pageSize);

    Activity adminDetail(Long id);

    /** 新增活动，content 入库前清洗 */
    Long create(Activity entity, Long publisherId);

    /** 更新活动，仅覆盖非空字段，content 入库前清洗 */
    void update(Activity entity);

    /** 删除活动（同时清理其报名记录） */
    void remove(Long id);

    /** 某活动的报名记录分页（含活动标题与老人姓名） */
    PageResult<ActivitySignup> adminSignups(Long activityId, Integer pageNum, Integer pageSize);

    /** 管理员调整报名状态 */
    void updateSignup(Long signupId, Integer status);
}
