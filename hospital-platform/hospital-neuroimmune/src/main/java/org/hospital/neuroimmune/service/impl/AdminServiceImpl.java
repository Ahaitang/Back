package org.hospital.neuroimmune.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.hospital.neuroimmune.dto.LoginRequest;
import org.hospital.neuroimmune.entity.Admin;
import org.hospital.neuroimmune.mapper.AdminMapper;
import org.hospital.neuroimmune.service.AdminService;
import org.hospital.neuroimmune.util.PasswordUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AdminServiceImpl implements AdminService {

    private static final Logger logger = LoggerFactory.getLogger(AdminServiceImpl.class);

    @Autowired
    private AdminMapper adminMapper;

    @Override
    public Admin login(LoginRequest request) {
        logger.info("Admin登录查询: username={}", request.getUsername());
        Admin admin = adminMapper.selectByUsername(request.getUsername());

        if (admin == null) {
            logger.warn("Admin用户不存在: {}", request.getUsername());
            return null;
        }

        logger.info("找到Admin用户: id={}, username={}", admin.getId(), admin.getUsername());
        boolean passwordMatch = PasswordUtil.matches(request.getPassword(), admin.getPassword());
        logger.info("密码验证结果: {}", passwordMatch ? "匹配" : "不匹配");

        if (passwordMatch) {
            return admin;
        }
        return null;
    }

    @Override
    public Admin getById(Long id) {
        return adminMapper.selectById(id);
    }

    @Override
    public void updatePassword(Long id, String password) {
        LambdaUpdateWrapper<Admin> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Admin::getId, id)
                .set(Admin::getPassword, PasswordUtil.encode(password));
        adminMapper.update(null, updateWrapper);
    }
}