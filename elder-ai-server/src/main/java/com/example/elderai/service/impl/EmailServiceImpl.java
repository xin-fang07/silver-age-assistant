package com.example.elderai.service.impl;

import com.example.elderai.service.EmailService;
import jakarta.annotation.Resource;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 邮件通知服务实现类（基于 Spring Boot Mail）
 * <p>
 * 通过 SMTP 协议向指定邮箱发送紧急求助通知邮件。
 * 配置项在 application.yml 的 spring.mail 和 app.mail 节点下。
 * </p>
 * <p>
 * 支持的邮箱：QQ邮箱、163邮箱、Gmail 等。
 * QQ邮箱开通SMTP步骤：登录QQ邮箱 → 设置 → 账户 → POP3/SMTP服务 → 开启 → 获取授权码
 * </p>
 */
@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);

    /** Spring Boot 自动配置的邮件发送器 */
    @Resource
    private JavaMailSender mailSender;

    /** 发件邮箱地址（从 spring.mail.username 读取） */
    @Value("${spring.mail.username:}")
    private String fromEmail;

    /** 默认通知接收邮箱（从 app.mail.notification-email 读取） */
    @Value("${app.mail.notification-email:}")
    private String defaultNotificationEmail;

    @Override
    public boolean sendEmergencyEmail(String toEmail, String fromUser, String fromPhone, String contactName, String contactPhone, String helpContent) {
        // 1. 检查发件邮箱配置是否完整
        if (isEmpty(fromEmail) || fromEmail.startsWith("your-")) {
            log.warn("邮件服务配置不完整（spring.mail.username 未配置），跳过发送。请在 application.yml 中配置 spring.mail 相关参数。");
            return false;
        }

        // 2. 确定收件人：优先使用传入的邮箱，为空时使用默认通知邮箱
        String recipient = isEmpty(toEmail) ? defaultNotificationEmail : toEmail;
        if (isEmpty(recipient) || recipient.startsWith("your-")) {
            log.warn("未指定收件邮箱，且默认通知邮箱（app.mail.notification-email）未配置。");
            return false;
        }

        try {
            // 3. 创建 HTML 邮件
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(recipient);
            helper.setSubject("【紧急求助】来自" + safe(fromUser) + "的紧急求助");

            // 4. 构建 HTML 邮件内容
            String timestamp = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            String htmlContent = buildEmailHtml(fromUser, fromPhone, contactName, contactPhone, helpContent, timestamp);
            helper.setText(htmlContent, true);

            // 5. 发送邮件
            mailSender.send(message);

            log.info("紧急求助邮件发送成功，收件地址未记录");
            return true;

        } catch (MessagingException e) {
            log.error("邮件发送失败，异常类型={}", e.getClass().getSimpleName());
            return false;
        } catch (Exception e) {
            log.error("邮件发送异常，异常类型={}", e.getClass().getSimpleName());
            return false;
        }
    }

    /**
     * 构建紧急求助邮件的 HTML 内容
     */
    private String buildEmailHtml(String fromUser, String fromPhone, String contactName, String contactPhone, String helpContent, String timestamp) {
        String from = safe(fromUser);
        String fromPhoneSafe = safe(fromPhone);
        String contact = safe(contactName);
        String phone = safe(contactPhone);
        String content = safe((helpContent != null && !helpContent.isBlank())
                ? helpContent : "我遇到了紧急情况，请尽快联系我！");

        return "<div style='max-width:600px;margin:0 auto;font-family:Microsoft YaHei,Arial,sans-serif;'>"
                + "<h2 style='color:#e74c3c;border-bottom:2px solid #e74c3c;padding-bottom:10px;'>紧急求助通知</h2>"
                + "<p style='font-size:16px;line-height:1.8;'>您的家人 <strong style='color:#2c3e50;font-size:18px;'>"
                + from + "</strong> 发来紧急求助：</p>"
                + "<p style='font-size:16px;line-height:1.8;'>求助人电话：<strong style='color:#2c3e50;font-size:16px;'>"
                + fromPhoneSafe + "</strong></p>"
                + "<blockquote style='background:#fdf2f2;border-left:4px solid #e74c3c;padding:15px 20px;margin:20px 0;font-size:16px;color:#333;line-height:1.8;'>"
                + content + "</blockquote>"
                + "<p style='font-size:16px;line-height:1.8;'>紧急联系人：<strong style='color:#2c3e50;font-size:16px;'>"
                + contact + "</strong></p>"
                + "<p style='font-size:16px;line-height:1.8;'>紧急联系电话：<strong style='color:#e74c3c;font-size:20px;'>"
                + phone + "</strong></p>"
                + "<p style='color:#999;font-size:14px;'>发送时间：" + timestamp + "</p>"
                + "<hr style='border:none;border-top:1px solid #eee;margin:20px 0;'>"
                + "<p style='color:#999;font-size:12px;'>此邮件由银发智能生活助手系统自动发送，请尽快联系您的家人。</p>"
                + "</div>";
    }

    /** 安全地处理字符串，null 返回默认值 */
    private String safe(String str) {
        String value = (str == null || str.isBlank()) ? "未设置" : str;
        return HtmlUtils.htmlEscape(value);
    }

    /** 判断字符串是否为空 */
    private boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    @Override
    public boolean sendResetPasswordEmail(String toEmail, String code) {
        if (isEmpty(fromEmail) || fromEmail.startsWith("your-")) {
            log.warn("邮件服务配置不完整（spring.mail.username 未配置），跳过发送。");
            return false;
        }

        if (isEmpty(toEmail)) {
            log.warn("未指定收件邮箱");
            return false;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("【银发智能生活助手】密码重置验证码");

            String timestamp = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            String htmlContent = buildResetPasswordHtml(code, timestamp);
            helper.setText(htmlContent, true);

            mailSender.send(message);

            log.info("密码重置邮件发送成功");
            return true;

        } catch (MessagingException e) {
            log.error("密码重置邮件发送失败，异常类型={}", e.getClass().getSimpleName());
            return false;
        } catch (Exception e) {
            log.error("密码重置邮件发送异常，异常类型={}", e.getClass().getSimpleName());
            return false;
        }
    }

    private String buildResetPasswordHtml(String code, String timestamp) {
        return "<div style='max-width:600px;margin:0 auto;font-family:Microsoft YaHei,Arial,sans-serif;'>"
                + "<h2 style='color:#4a90d9;border-bottom:2px solid #4a90d9;padding-bottom:10px;'>密码重置</h2>"
                + "<p style='font-size:16px;line-height:1.8;'>您好！您正在申请重置银发智能生活助手的登录密码。</p>"
                + "<p style='font-size:16px;line-height:1.8;'>您的验证码是：</p>"
                + "<div style='background:#f0f7ff;border:2px solid #4a90d9;border-radius:10px;padding:20px;text-align:center;margin:20px 0;'>"
                + "<span style='font-size:36px;font-weight:bold;color:#4a90d9;letter-spacing:8px;'>" + safe(code) + "</span>"
                + "</div>"
                + "<p style='font-size:16px;line-height:1.8;'>请在 5 分钟内输入此验证码完成密码重置。</p>"
                + "<p style='font-size:14px;line-height:1.8;color:#999;'>如果这不是您本人操作，请忽略此邮件，您的密码不会被更改。</p>"
                + "<p style='color:#999;font-size:14px;'>发送时间：" + timestamp + "</p>"
                + "<hr style='border:none;border-top:1px solid #eee;margin:20px 0;'>"
                + "<p style='color:#999;font-size:12px;'>此邮件由银发智能生活助手系统自动发送，请勿回复。</p>"
                + "</div>";
    }
}
