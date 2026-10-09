package com.example.elderai.config;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.elderai.entity.User;
import com.example.elderai.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Profile;

/**
 * 数据库初始化组件
 * <p>
 * 在项目启动时自动执行，检查并创建默认的管理员用户和测试老年用户。
 * 如果用户已存在则跳过创建。
 * </p>
 */
@Component
@Profile("dev")
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        logger.info("开始检查本地开发账号初始化状态");

        initAdminUser();

        logger.info("本地开发账号初始化检查完成");
    }

    /**
     * 初始化管理员用户
     */
    private void initAdminUser() {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", "admin");
        User admin = userMapper.selectOne(queryWrapper);

        if (admin == null) {
            admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("123456"));
            admin.setRole("ADMIN");
            admin.setStatus(1);
            userMapper.insert(admin);
            logger.info("[初始化] 管理员用户已创建 -> 用户名: admin, 角色: ADMIN");
        } else {
            admin.setPassword(passwordEncoder.encode("123456"));
            userMapper.updateById(admin);
            logger.info("[初始化] 管理员密码已更新 -> 用户名: admin");
        }
    }

    /**
     * 初始化测试家属用户
     */
    private void initTestFamilyUser() {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", "test");
        User existingUser = userMapper.selectOne(queryWrapper);

        if (existingUser == null) {
            User testUser = new User();
            testUser.setUsername("test");
            testUser.setPassword(passwordEncoder.encode("Test@123456"));
            testUser.setRole("FAMILY");
            testUser.setPhone("13800001111");
            testUser.setStatus(1);
            userMapper.insert(testUser);
            logger.info("[初始化] 测试家属用户已创建 -> 用户名: test, 角色: FAMILY");
        } else {
            if ("ELDER".equals(existingUser.getRole())) {
                existingUser.setRole("FAMILY");
                userMapper.updateById(existingUser);
                logger.info("[初始化] 测试用户角色已更新 -> 用户名: test, 角色: ELDER -> FAMILY");
            } else {
                logger.debug("[初始化] 测试家属用户已存在，跳过创建");
            }
        }
    }
}
