package com.example.elderai.service;

/**
 * 邮件通知服务接口
 * <p>
 * 用于在紧急求助等场景下向紧急联系人或默认通知邮箱发送邮件通知。
 * </p>
 */
public interface EmailService {

    /**
     * 发送紧急求助邮件
     * <p>
     * 向指定邮箱（或默认通知邮箱）发送一封紧急求助通知邮件。
     * 邮件为 HTML 格式，包含求助人姓名、联系电话和求助内容。
     * </p>
     *
     * @param toEmail       收件人邮箱（为空时发送到 application.yml 中配置的默认通知邮箱）
     * @param fromUser      发起求助的老年用户姓名（优先真实姓名，否则用户名）
     * @param fromPhone     发起求助的老年用户电话
     * @param contactName   紧急联系人姓名
     * @param contactPhone  紧急联系电话
     * @param helpContent   求助内容
     * @return 发送成功返回 true，失败返回 false
     */
    boolean sendEmergencyEmail(String toEmail, String fromUser, String fromPhone, String contactName, String contactPhone, String helpContent);

    /**
     * 发送密码重置邮件
     * <p>
     * 向用户邮箱发送密码重置验证码邮件。
     * </p>
     *
     * @param toEmail 收件人邮箱
     * @param code    验证码
     * @return 发送成功返回 true，失败返回 false
     */
    boolean sendResetPasswordEmail(String toEmail, String code);
}
