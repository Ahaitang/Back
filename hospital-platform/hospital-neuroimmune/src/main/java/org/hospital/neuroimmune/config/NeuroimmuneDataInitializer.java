package org.hospital.neuroimmune.config;

import org.hospital.neuroimmune.entity.Admin;
import org.hospital.neuroimmune.mapper.AdminMapper;
import org.hospital.common.util.PasswordUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

/**
 * 系统启动初始化
 */
@Component
public class NeuroimmuneDataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(NeuroimmuneDataInitializer.class);

    @Autowired
    private AdminMapper adminMapper;

    // 从环境变量读取默认密码，如果没有则使用随机密码
    @Value("${admin.default.password:}")
    private String defaultPassword;

    @Override
    public void run(String... args) {
        initAdmin();
    }

    /**
     * 初始化管理员账号
     */
    private void initAdmin() {
        try {
            Admin existingAdmin = adminMapper.selectByUsername("admin");
            if (existingAdmin == null) {
                Admin admin = new Admin();
                admin.setUsername("admin");
                // 如果环境变量配置了密码则使用，否则生成随机密码
                String password = (defaultPassword != null && !defaultPassword.isEmpty())
                    ? defaultPassword
                    : generateRandomPassword();
                admin.setPassword(PasswordUtil.encode(password));
                admin.setName("系统管理员");
                admin.setLevel(1);  // 超级管理员
                adminMapper.insert(admin);
                logger.info("已自动创建默认管理员账号: admin / {}", password);
                logger.warn("请登录后立即修改默认密码！");
            } else {
                logger.info("管理员账号已存在");
            }
        } catch (Exception e) {
            logger.error("初始化管理员账号失败: {}", e.getMessage());
        }
    }

    /**
     * 生成随机密码（8位）
     */
    private String generateRandomPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        java.security.SecureRandom random = new java.security.SecureRandom();
        for (int i = 0; i < 8; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}