package com.example.elderai.service.impl;

import com.example.elderai.common.BusinessException;
import com.example.elderai.dto.ForgotPasswordDTO;
import com.example.elderai.dto.ResetPasswordDTO;
import com.example.elderai.entity.User;
import com.example.elderai.mapper.UserMapper;
import com.example.elderai.service.EmailService;
import com.example.elderai.service.PasswordResetService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class PasswordResetServiceImpl implements PasswordResetService {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetServiceImpl.class);

    private static final int CODE_LENGTH = 6;
    private static final int CODE_EXPIRE_MINUTES = 5;
    private static final int MAX_SEND_INTERVAL_SECONDS = 60;

    private record CodeInfo(String code, LocalDateTime createTime) {}

    private final Map<String, CodeInfo> codeCache = new ConcurrentHashMap<>();
    private final Map<String, LocalDateTime> lastSendTime = new ConcurrentHashMap<>();

    @Resource
    private UserMapper userMapper;

    @Resource
    private EmailService emailService;

    @Resource
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    public void sendResetCode(ForgotPasswordDTO dto) {
        String email = dto.getEmail().toLowerCase().trim();

        User user = userMapper.selectOne(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<User>()
                .eq("email", email));
        if (user == null) {
            throw new BusinessException(404, "该邮箱未注册");
        }

        LocalDateTime lastSend = lastSendTime.get(email);
        if (lastSend != null && lastSend.plusSeconds(MAX_SEND_INTERVAL_SECONDS).isAfter(LocalDateTime.now())) {
            throw new BusinessException(400, "发送过于频繁，请稍后再试");
        }

        String code = generateCode();
        codeCache.put(email, new CodeInfo(code, LocalDateTime.now()));
        lastSendTime.put(email, LocalDateTime.now());

        boolean success = emailService.sendResetPasswordEmail(email, code);
        if (!success) {
            codeCache.remove(email);
            throw new BusinessException(500, "邮件发送失败，请稍后重试");
        }

        scheduleCleanup(email);
        log.info("密码重置验证码已发送到邮箱: {}", email);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(ResetPasswordDTO dto) {
        String email = dto.getEmail().toLowerCase().trim();
        String code = dto.getCode().trim();

        CodeInfo codeInfo = codeCache.get(email);
        if (codeInfo == null) {
            throw new BusinessException(400, "验证码已过期，请重新获取");
        }

        if (codeInfo.createTime().plusMinutes(CODE_EXPIRE_MINUTES).isBefore(LocalDateTime.now())) {
            codeCache.remove(email);
            throw new BusinessException(400, "验证码已过期，请重新获取");
        }

        if (!code.equals(codeInfo.code())) {
            throw new BusinessException(400, "验证码不正确");
        }

        User user = userMapper.selectOne(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<User>()
                .eq("email", email));
        if (user == null) {
            throw new BusinessException(404, "该邮箱未注册");
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);

        codeCache.remove(email);
        lastSendTime.remove(email);

        log.info("用户 [{}] 密码重置成功", user.getUsername());
    }

    private String generateCode() {
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    private void scheduleCleanup(String email) {
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.schedule(() -> {
            codeCache.remove(email);
            executor.shutdown();
        }, CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);
    }
}