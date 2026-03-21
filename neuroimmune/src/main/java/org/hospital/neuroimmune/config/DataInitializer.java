package org.hospital.neuroimmune.config;

import org.hospital.neuroimmune.entity.Admin;
import org.hospital.neuroimmune.mapper.AdminMapper;
import org.hospital.neuroimmune.util.PasswordUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 系统启动初始化
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private AdminMapper adminMapper;

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
                admin.setPassword(PasswordUtil.encode("123456"));
                admin.setName("系统管理员");
                admin.setLevel(1);  // 超级管理员
                adminMapper.insert(admin);
                logger.info("已自动创建默认管理员账号: admin / 123456");
            } else {
                logger.info("管理员账号已存在");
            }
        } catch (Exception e) {
            logger.error("初始化管理员账号失败: {}", e.getMessage());
        }
    }
}