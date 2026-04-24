package org.hospital.neuroimmune.config;

import org.hospital.neuroimmune.entity.Doctor;
import org.hospital.neuroimmune.mapper.NeuroimmuneDoctorMapper;
import org.hospital.neuroimmune.service.DoctorRoleService;
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
    private NeuroimmuneDoctorMapper doctorMapper;

    @Autowired
    private DoctorRoleService doctorRoleService;

    @Value("${admin.default.password:}")
    private String defaultPassword;

    @Override
    public void run(String... args) {
        initAdmin();
    }

    private void initAdmin() {
        try {
            Doctor existingAdmin = doctorMapper.selectByPhone("admin");
            if (existingAdmin == null) {
                Doctor admin = new Doctor();
                admin.setPhone("admin");
                admin.setName("系统管理员");
                String password = (defaultPassword != null && !defaultPassword.isEmpty())
                    ? defaultPassword : generateRandomPassword();
                admin.setPassword(PasswordUtil.encode(password));
                admin.setLevel(1);
                admin.setHospital("系统管理");
                admin.setDepartment("管理部");
                doctorMapper.insert(admin);
                // 添加 ADMIN 角色
                doctorRoleService.addRoleToDoctor(admin.getId(), "ADMIN");
                logger.info("已自动创建默认管理员账号: admin / {}", password);
                logger.warn("请登录后立即修改默认密码！");
            } else {
                logger.info("管理员账号已存在");
            }
        } catch (Exception e) {
            logger.error("初始化管理员账号失败: {}", e.getMessage());
        }
    }

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