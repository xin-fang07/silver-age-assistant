package com.example.elderai.service.impl;

import com.example.elderai.entity.ElderInfo;
import com.example.elderai.entity.EmergencyHelp;
import com.example.elderai.entity.EmergencyNotification;
import com.example.elderai.entity.FamilyBinding;
import com.example.elderai.entity.Notification;
import com.example.elderai.entity.User;
import com.example.elderai.mapper.FamilyBindingMapper;
import com.example.elderai.mapper.NotificationMapper;
import java.util.List;
import java.util.Map;
import com.example.elderai.mapper.ElderInfoMapper;
import com.example.elderai.mapper.UserMapper;
import com.example.elderai.mapper.EmergencyHelpMapper;
import com.example.elderai.mapper.EmergencyNotificationMapper;
import com.example.elderai.service.EmailService;
import com.example.elderai.service.EmergencyNotificationService;
import com.example.elderai.handler.ReminderWebSocketHandler;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class EmergencyNotificationServiceImpl implements EmergencyNotificationService {

    public static final int NOT_CONFIGURED = 0;
    public static final int SENT = 2;
    public static final int FAILED = 3;

    @Resource(name = "emergencyNotificationMapper")
    private EmergencyNotificationMapper notificationMapper;

    @Resource
    private NotificationMapper familyNotificationMapper;

    @Resource
    private EmergencyHelpMapper helpMapper;

    @Resource
    private EmailService emailService;

    @Resource
    private UserMapper userMapper;

    @Resource
    private ElderInfoMapper elderInfoMapper;

    @Resource
    private FamilyBindingMapper familyBindingMapper;

    @Resource
    private ReminderWebSocketHandler webSocketHandler;

    /** 全局默认通知邮箱：联系人邮箱未填时回落使用，配置项 app.mail.notification-email */
    @Value("${app.mail.notification-email:}")
    private String defaultNotificationEmail;

    @Override
    public void notifyCreated(EmergencyHelp help) {
        record(help.getId(), "ADMIN_DASHBOARD", "ADMIN", "SENT", null);

        String fromUser = resolveFromUser(help.getUserId());
        String fromPhone = resolveFromPhone(help.getUserId());
        boolean emailSent = false;

        // 通知求助表单中填写的联系人邮箱
        String contactEmail = help.getContactEmail();
        if (contactEmail == null || contactEmail.isBlank()) {
            contactEmail = defaultNotificationEmail;
        }
        if (contactEmail != null && !contactEmail.isBlank()) {
            boolean sent = emailService.sendEmergencyEmail(
                    contactEmail, fromUser, fromPhone, help.getContactName(), help.getContactPhone(), help.getHelpContent());
            if (sent) {
                emailSent = true;
                record(help.getId(), "EMAIL", contactEmail, "SENT", null);
            } else {
                record(help.getId(), "EMAIL", contactEmail, "FAILED", "邮件服务未配置或发送失败");
            }
        } else {
            record(help.getId(), "EMAIL", "未配置", "SKIPPED", "未设置联系人邮箱且未配置默认通知邮箱");
        }

        // 通知个人资料中设置的紧急联系人（按当前家属绑定的首位老人档案解析）
        Long elderInfoId = resolveElderOfFamily(help.getUserId());
        ElderInfo elderInfo = elderInfoId != null ? elderInfoMapper.selectById(elderInfoId) : null;
        if (elderInfo != null && elderInfo.getEmergencyContact() != null && !elderInfo.getEmergencyContact().isBlank()) {
            String emergencyName = elderInfo.getEmergencyContact();
            String emergencyPhone = elderInfo.getEmergencyPhone();
            String emergencyEmail = defaultNotificationEmail;

            if (emergencyEmail != null && !emergencyEmail.isBlank()) {
                boolean sent = emailService.sendEmergencyEmail(
                        emergencyEmail, fromUser, fromPhone, emergencyName, emergencyPhone, help.getHelpContent());
                if (sent) {
                    emailSent = true;
                    record(help.getId(), "EMAIL", emergencyEmail, "SENT", "紧急联系人通知");
                } else {
                    record(help.getId(), "EMAIL", emergencyEmail, "FAILED", "紧急联系人邮件发送失败");
                }
            } else {
                record(help.getId(), "EMAIL", "未配置", "SKIPPED", "紧急联系人邮箱未配置");
            }
        }

        if (emailSent) {
            updateNotificationResult(help, SENT, LocalDateTime.now(), "平台已收到，通知邮件已发送");
        } else {
            updateNotificationResult(help, FAILED, null, "平台已收到，但邮件通知发送失败");
        }

        notifyBoundFamilies(help);
    }

    /**
     * 解析触发求助的家属所绑定的首位老人档案ID（老人不再作为登录用户，
     * 通过 family_binding 关联；无绑定返回 null）。
     */
    private Long resolveElderOfFamily(Long familyUserId) {
        if (familyUserId == null) {
            return null;
        }
        FamilyBinding binding = familyBindingMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<FamilyBinding>()
                        .eq("family_user_id", familyUserId).eq("status", 1).last("LIMIT 1"));
        return binding != null ? binding.getElderInfoId() : null;
    }

    private void notifyBoundFamilies(EmergencyHelp help) {
        Long elderInfoId = resolveElderOfFamily(help.getUserId());
        List<FamilyBinding> bound = familyBindingMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<FamilyBinding>()
                        .eq("elder_info_id", elderInfoId).eq("status", 1).ne("family_user_id", help.getUserId()));
        if (bound == null || bound.isEmpty()) {
            return;
        }
        String elderName = "老人";
        if (elderInfoId != null) {
            ElderInfo ei = elderInfoMapper.selectById(elderInfoId);
            if (ei != null) {
                elderName = (ei.getRealName() != null && !ei.getRealName().isBlank()) ? ei.getRealName()
                        : (ei.getNickname() != null && !ei.getNickname().isBlank() ? ei.getNickname() : "老人");
            }
        }
        String content = elderName + " 发起了紧急求助"
                + (help.getHelpContent() != null ? "：" + help.getHelpContent() : "")
                + "，请尽快关注并处理！";
        for (FamilyBinding fb : bound) {
            Notification n = new Notification();
            n.setUserId(fb.getFamilyUserId());
            n.setType("SOS");
            n.setTitle("家人紧急求助");
            n.setContent(content);
            n.setRefId(help.getId());
            n.setIsRead(0);
            familyNotificationMapper.insert(n);
            webSocketHandler.sendEventToUser(fb.getFamilyUserId(), "SOS_CREATED", Map.of(
                    "notificationId", n.getId(),
                    "emergencyId", help.getId(),
                    "elderId", elderInfoId,
                    "elderName", elderName,
                    "title", n.getTitle(),
                    "content", n.getContent()
            ));
            record(help.getId(), "FAMILY_APP", fb.getFamilyUserId().toString(), "SENT", "家属站内通知");
        }
    }

    @Override
    public void recordEscalation(EmergencyHelp help) {
        record(help.getId(), "ADMIN_ESCALATION", "ADMIN", "SENT",
                "第" + help.getEscalationLevel() + "次超时升级");
    }

    @Override
    public void notifyEscalation(EmergencyHelp help) {
        Long elderInfoId = resolveElderOfFamily(help.getUserId());
        String levelText = "第" + help.getEscalationLevel() + "次超时升级";
        String contactEmail = help.getContactEmail();
        if (contactEmail == null || contactEmail.isBlank()) contactEmail = defaultNotificationEmail;
        if (contactEmail != null && !contactEmail.isBlank()) {
            boolean sent = emailService.sendEmergencyEmail(contactEmail,
                    resolveFromUser(help.getUserId()), resolveFromPhone(help.getUserId()),
                    help.getContactName(), help.getContactPhone(), levelText + "：" + help.getHelpContent());
            record(help.getId(), "EMAIL_ESCALATION", contactEmail,
                    sent ? "SENT" : "FAILED", sent ? levelText : "升级邮件发送失败");
        }

        List<FamilyBinding> bound = familyBindingMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<FamilyBinding>()
                        .eq("elder_info_id", elderInfoId).eq("status", 1));
        for (FamilyBinding fb : bound) {
            Notification n = new Notification();
            n.setUserId(fb.getFamilyUserId());
            n.setType("SOS");
            n.setTitle("紧急求助超时升级");
            n.setContent(resolveFromUser(help.getUserId()) + "的求助仍无人确认，" + levelText + "，请立即处理！");
            n.setRefId(help.getId());
            n.setIsRead(0);
            n.setCreateTime(LocalDateTime.now());
            familyNotificationMapper.insert(n);
            webSocketHandler.sendEventToUser(fb.getFamilyUserId(), "SOS_ESCALATED", Map.of(
                    "notificationId", n.getId(), "emergencyId", help.getId(),
                    "elderId", elderInfoId, "title", n.getTitle(), "content", n.getContent()
            ));
            record(help.getId(), "FAMILY_ESCALATION", fb.getFamilyUserId().toString(), "SENT", levelText);
        }
    }

    private void updateNotificationResult(EmergencyHelp help, int status,
                                          LocalDateTime notifiedAt, String message) {
        help.setNotificationStatus(status);
        help.setNotifiedAt(notifiedAt);
        help.setNotificationMessage(message);
        helpMapper.updateById(help);
    }

    private void record(Long helpId, String channel, String recipient,
                        String status, String errorMessage) {
        EmergencyNotification notification = new EmergencyNotification();
        notification.setHelpId(helpId);
        notification.setChannel(channel);
        notification.setRecipient(recipient);
        notification.setStatus(status);
        notification.setAttemptNo(1);
        notification.setErrorMessage(errorMessage);
        notification.setCreateTime(LocalDateTime.now());
        if ("SENT".equals(status)) {
            notification.setSentTime(LocalDateTime.now());
        }
        notificationMapper.insert(notification);
    }

    /**
     * 根据 userId 解析发起求助的老年用户姓名
     * 优先使用 ElderInfo.realName，未设置则使用 User.username
     */
    private String resolveFromUser(Long userId) {
        if (userId == null) {
            return "未设置";
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            return "未设置";
        }
        return user.getUsername();
    }

    /**
     * 根据 userId 解析发起求助的老年用户电话
     */
    private String resolveFromPhone(Long userId) {
        if (userId == null) {
            return null;
        }
        User user = userMapper.selectById(userId);
        return user != null ? user.getPhone() : null;
    }
}
