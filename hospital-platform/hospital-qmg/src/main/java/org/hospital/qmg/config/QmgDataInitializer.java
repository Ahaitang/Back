package org.hospital.qmg.config;

import lombok.extern.slf4j.Slf4j;
import org.hospital.common.util.CredentialFileWriter;
import org.hospital.common.util.PasswordUtil;
import org.hospital.qmg.entity.Doctor;
import org.hospital.qmg.mapper.QmgDoctorMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 数据初始化器
 * 应用启动时自动执行，初始化默认数据
 */
@Slf4j
@Component
public class QmgDataInitializer implements CommandLineRunner {

    @Autowired
    private QmgDoctorMapper doctorMapper;

    // 从环境变量读取默认密码
    @Value("${admin.default.password:}")
    private String defaultPassword;

    @Override
    public void run(String... args) throws Exception {
        log.info("开始初始化默认数据...");
        initAdminUser();
        log.info("默认数据初始化完成");
    }

    /**
     * 初始化 admin 用户
     * 如果不存在则创建，如果存在则不修改
     */
    private void initAdminUser() {
        try {
            // 检查 admin 用户是否已存在
            Doctor existingAdmin = doctorMapper.findByUsername("admin");

            if (existingAdmin == null) {
                // 创建 admin 用户
                Doctor admin = new Doctor();
                admin.setEmployeeNumber("EMP000001");
                admin.setUsername("admin");
                // 如果环境变量配置了密码则使用，否则生成随机密码
                String password = (defaultPassword != null && !defaultPassword.isEmpty())
                    ? defaultPassword
                    : generateRandomPassword();
                admin.setPassword(PasswordUtil.encode(password));
                admin.setLevel(0); // 超级管理员
                admin.setCreateTime(LocalDateTime.now());
                admin.setUpdateTime(LocalDateTime.now());

                doctorMapper.insert(admin);
                log.info("✅ 成功创建默认管理员账号: username=admin, password={}, employeeNumber=EMP000001", password);
                log.warn("⚠️ 请登录后立即修改默认密码！");
                CredentialFileWriter.writeCredential("QMG 评分系统", "admin", password);
            } else {
                // 检查是否需要更新工号（兼容旧数据）
                if (existingAdmin.getEmployeeNumber() == null || existingAdmin.getEmployeeNumber().trim().isEmpty()) {
                    existingAdmin.setEmployeeNumber("EMP000001");
                    existingAdmin.setUpdateTime(LocalDateTime.now());
                    doctorMapper.update(existingAdmin);
                    log.info("✅ 已更新 admin 用户的工号: EMP000001");
                } else {
                    log.info("ℹ️  admin 用户已存在，跳过创建: username=admin, employeeNumber={}", existingAdmin.getEmployeeNumber());
                }
            }
        } catch (Exception e) {
            log.error("❌ 初始化 admin 用户失败: {}", e.getMessage(), e);
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
