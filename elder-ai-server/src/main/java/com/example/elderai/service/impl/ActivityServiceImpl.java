package com.example.elderai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.elderai.common.BusinessException;
import com.example.elderai.common.HtmlSanitizer;
import com.example.elderai.common.PageResult;
import com.example.elderai.entity.Activity;
import com.example.elderai.entity.ActivitySignup;
import com.example.elderai.entity.ElderInfo;
import com.example.elderai.entity.FamilyBinding;
import com.example.elderai.mapper.ActivityMapper;
import com.example.elderai.mapper.ActivitySignupMapper;
import com.example.elderai.mapper.ElderInfoMapper;
import com.example.elderai.mapper.FamilyBindingMapper;
import com.example.elderai.service.ActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * 娱乐活动服务实现类。
 *
 * @author elder-ai-team
 */
@Service
public class ActivityServiceImpl implements ActivityService {

    private static final Integer STATUS_PUBLISHED = 1;
    private static final String TYPE_OFFLINE = "OFFLINE";
    private static final Integer SIGNUP_ACTIVE = 0;
    private static final Integer SIGNUP_CANCELLED = 1;

    @Autowired
    private ActivityMapper activityMapper;
    @Autowired
    private ActivitySignupMapper signupMapper;
    @Autowired
    private FamilyBindingMapper familyBindingMapper;
    @Autowired
    private ElderInfoMapper elderInfoMapper;

    // ---------------- 家属端 ----------------

    @Override
    public PageResult<Activity> familyList(Long familyUserId, String type, String keyword, Integer pageNum, Integer pageSize) {
        Page<Activity> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Activity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Activity::getStatus, STATUS_PUBLISHED);
        if (StringUtils.hasText(type)) {
            wrapper.eq(Activity::getType, type);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Activity::getTitle, keyword);
        }
        wrapper.orderByDesc(Activity::getCreateTime);
        Page<Activity> resultPage = activityMapper.selectPage(page, wrapper);
        return PageResult.pageSuccess(resultPage.getRecords(), resultPage.getTotal(),
                resultPage.getCurrent(), resultPage.getSize());
    }

    @Override
    public Activity familyDetail(Long id) {
        Activity activity = activityMapper.selectById(id);
        if (activity == null || !STATUS_PUBLISHED.equals(activity.getStatus())) {
            throw new BusinessException(404, "活动不存在或已下架");
        }
        return activity;
    }

    @Override
    public Long signup(Long activityId, Long familyUserId, Long elderInfoId, String contactPhone, String remark) {
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null || !STATUS_PUBLISHED.equals(activity.getStatus())) {
            throw new BusinessException(404, "活动不存在或已下架");
        }
        if (!TYPE_OFFLINE.equals(activity.getType())) {
            throw new BusinessException(400, "该活动不支持报名");
        }
        if (activity.getSignupDeadline() != null && activity.getSignupDeadline().isBefore(LocalDateTime.now())) {
            throw new BusinessException(400, "报名已截止");
        }
        if (activity.getCapacity() != null && activity.getCapacity() > 0
                && (activity.getSignupCount() == null || activity.getSignupCount() >= activity.getCapacity())) {
            throw new BusinessException(400, "报名人数已满");
        }
        if (elderInfoId != null) {
            assertBound(familyUserId, elderInfoId);
        }
        Long dup = signupMapper.selectCount(new LambdaQueryWrapper<ActivitySignup>()
                .eq(ActivitySignup::getActivityId, activityId)
                .eq(ActivitySignup::getFamilyUserId, familyUserId)
                .eq(elderInfoId != null, ActivitySignup::getElderInfoId, elderInfoId)
                .eq(ActivitySignup::getStatus, SIGNUP_ACTIVE));
        if (dup != null && dup > 0) {
            throw new BusinessException(400, "您已为该老人报名此活动");
        }
        ActivitySignup signup = new ActivitySignup();
        signup.setActivityId(activityId);
        signup.setFamilyUserId(familyUserId);
        signup.setElderInfoId(elderInfoId);
        signup.setContactPhone(contactPhone);
        signup.setRemark(remark);
        signup.setStatus(SIGNUP_ACTIVE);
        signup.setCreateTime(LocalDateTime.now());
        signupMapper.insert(signup);
        activity.setSignupCount((activity.getSignupCount() == null ? 0 : activity.getSignupCount()) + 1);
        activityMapper.updateById(activity);
        return signup.getId();
    }

    @Override
    public PageResult<ActivitySignup> mySignups(Long familyUserId, Integer pageNum, Integer pageSize) {
        Page<ActivitySignup> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ActivitySignup> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ActivitySignup::getFamilyUserId, familyUserId);
        wrapper.orderByDesc(ActivitySignup::getCreateTime);
        Page<ActivitySignup> resultPage = signupMapper.selectPage(page, wrapper);
        resultPage.getRecords().forEach(this::fillActivityTitle);
        resultPage.getRecords().forEach(this::fillElderName);
        return PageResult.pageSuccess(resultPage.getRecords(), resultPage.getTotal(),
                resultPage.getCurrent(), resultPage.getSize());
    }

    @Override
    public void cancelSignup(Long signupId, Long familyUserId) {
        ActivitySignup signup = signupMapper.selectById(signupId);
        if (signup == null) {
            throw new BusinessException(404, "报名记录不存在");
        }
        if (!familyUserId.equals(signup.getFamilyUserId())) {
            throw new BusinessException(403, "无权操作该报名记录");
        }
        if (SIGNUP_CANCELLED.equals(signup.getStatus())) {
            return;
        }
        signup.setStatus(SIGNUP_CANCELLED);
        signupMapper.updateById(signup);
        adjustCount(signup.getActivityId(), -1);
    }

    // ---------------- 管理员端 ----------------

    @Override
    public PageResult<Activity> adminList(String type, String keyword, Integer pageNum, Integer pageSize) {
        Page<Activity> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Activity> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(type)) {
            wrapper.eq(Activity::getType, type);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Activity::getTitle, keyword);
        }
        wrapper.orderByDesc(Activity::getCreateTime);
        Page<Activity> resultPage = activityMapper.selectPage(page, wrapper);
        return PageResult.pageSuccess(resultPage.getRecords(), resultPage.getTotal(),
                resultPage.getCurrent(), resultPage.getSize());
    }

    @Override
    public Activity adminDetail(Long id) {
        Activity activity = activityMapper.selectById(id);
        if (activity == null) {
            throw new BusinessException(404, "活动不存在");
        }
        return activity;
    }

    @Override
    public Long create(Activity entity, Long publisherId) {
        if (entity.getTitle() == null || !StringUtils.hasText(entity.getTitle().trim())) {
            throw new BusinessException(400, "请填写活动标题");
        }
        if (entity.getType() == null || !StringUtils.hasText(entity.getType().trim())) {
            throw new BusinessException(400, "请选择活动类型");
        }
        if (entity.getContent() != null) {
            entity.setContent(HtmlSanitizer.sanitize(entity.getContent()));
        }
        if (entity.getStatus() == null) {
            entity.setStatus(STATUS_PUBLISHED);
        }
        if (entity.getCapacity() == null) {
            entity.setCapacity(0);
        }
        if (entity.getSignupCount() == null) {
            entity.setSignupCount(0);
        }
        entity.setPublisherId(publisherId);
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        activityMapper.insert(entity);
        return entity.getId();
    }

    @Override
    public void update(Activity entity) {
        Activity existing = activityMapper.selectById(entity.getId());
        if (existing == null) {
            throw new BusinessException(404, "活动不存在");
        }
        if (entity.getTitle() != null) existing.setTitle(entity.getTitle());
        if (entity.getType() != null) existing.setType(entity.getType());
        if (entity.getCoverImage() != null) existing.setCoverImage(entity.getCoverImage());
        if (entity.getContent() != null) existing.setContent(HtmlSanitizer.sanitize(entity.getContent()));
        if (entity.getLocation() != null) existing.setLocation(entity.getLocation());
        if (entity.getStartTime() != null) existing.setStartTime(entity.getStartTime());
        if (entity.getEndTime() != null) existing.setEndTime(entity.getEndTime());
        if (entity.getSignupDeadline() != null) existing.setSignupDeadline(entity.getSignupDeadline());
        if (entity.getCapacity() != null) existing.setCapacity(entity.getCapacity());
        if (entity.getStatus() != null) existing.setStatus(entity.getStatus());
        existing.setUpdateTime(LocalDateTime.now());
        activityMapper.updateById(existing);
    }

    @Override
    public void remove(Long id) {
        if (activityMapper.selectById(id) == null) {
            throw new BusinessException(404, "活动不存在");
        }
        activityMapper.deleteById(id);
        signupMapper.delete(new LambdaQueryWrapper<ActivitySignup>().eq(ActivitySignup::getActivityId, id));
    }

    @Override
    public PageResult<ActivitySignup> adminSignups(Long activityId, Integer pageNum, Integer pageSize) {
        Page<ActivitySignup> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ActivitySignup> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ActivitySignup::getActivityId, activityId);
        wrapper.orderByDesc(ActivitySignup::getCreateTime);
        Page<ActivitySignup> resultPage = signupMapper.selectPage(page, wrapper);
        resultPage.getRecords().forEach(this::fillActivityTitle);
        resultPage.getRecords().forEach(this::fillElderName);
        return PageResult.pageSuccess(resultPage.getRecords(), resultPage.getTotal(),
                resultPage.getCurrent(), resultPage.getSize());
    }

    @Override
    public void updateSignup(Long signupId, Integer status) {
        ActivitySignup signup = signupMapper.selectById(signupId);
        if (signup == null) {
            throw new BusinessException(404, "报名记录不存在");
        }
        Integer oldStatus = signup.getStatus();
        signup.setStatus(status);
        signupMapper.updateById(signup);
        if (oldStatus != null && oldStatus.equals(SIGNUP_ACTIVE) && SIGNUP_CANCELLED.equals(status)) {
            adjustCount(signup.getActivityId(), -1);
        } else if (oldStatus != null && oldStatus.equals(SIGNUP_CANCELLED) && SIGNUP_ACTIVE.equals(status)) {
            adjustCount(signup.getActivityId(), 1);
        }
    }

    // ---------------- 私有方法 ----------------

    /** 报名计数增减（下限 0） */
    private void adjustCount(Long activityId, int delta) {
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null || activity.getSignupCount() == null) {
            return;
        }
        int next = activity.getSignupCount() + delta;
        if (next < 0) {
            next = 0;
        }
        activity.setSignupCount(next);
        activityMapper.updateById(activity);
    }

    /** 校验当前家属是否已绑定该老人（status=1） */
    private void assertBound(Long familyUserId, Long elderInfoId) {
        Long count = familyBindingMapper.selectCount(new LambdaQueryWrapper<FamilyBinding>()
                .eq(FamilyBinding::getFamilyUserId, familyUserId)
                .eq(FamilyBinding::getElderInfoId, elderInfoId)
                .eq(FamilyBinding::getStatus, 1));
        if (count == null || count == 0) {
            throw new BusinessException(403, "您尚未绑定该老人，无法为其报名");
        }
    }

    private void fillActivityTitle(ActivitySignup signup) {
        if (signup.getActivityId() != null) {
            Activity activity = activityMapper.selectById(signup.getActivityId());
            signup.setActivityTitle(activity != null ? activity.getTitle() : null);
        }
    }

    private void fillElderName(ActivitySignup signup) {
        if (signup.getElderInfoId() != null) {
            ElderInfo elder = elderInfoMapper.selectById(signup.getElderInfoId());
            signup.setElderName(elder != null ? elder.getRealName() : null);
        }
    }
}
