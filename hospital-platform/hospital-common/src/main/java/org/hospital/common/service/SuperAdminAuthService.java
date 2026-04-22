package org.hospital.common.service;

import lombok.extern.slf4j.Slf4j;
import org.hospital.common.config.SuperAdminConfig;
import org.hospital.common.security.JwtUtil;
import org.hospital.common.security.UserInfo;
import org.hospital.common.security.TokenStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 超级管理员认证服务
 */
@Slf4j
@Service
public class SuperAdminAuthService {

    @Autowired
    private SuperAdminConfig config;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private TokenStorage tokenStorage;

    /**
     * 登录验证
     * @return Token 或 null（失败）
     */
    public String login(String username, String password) {
        if (!config.validate(username, password)) {
            log.warn("超级管理员登录失败: username={}", username);
            return null;
        }

        SuperAdminConfig.Account account = config.findByUsername(username);

        // 创建 UserInfo，使用固定 userId=1L，role=super_admin，module=super-admin
        UserInfo userInfo = new UserInfo(1L, username, "super_admin", "super-admin");
        String token = jwtUtil.generateToken(userInfo);
        tokenStorage.storeToken(userInfo, token);

        log.info("超级管理员登录成功: username={}, name={}", username, account.getName());
        return token;
    }

    /**
     * 登出
     */
    public void logout(String username) {
        UserInfo userInfo = new UserInfo(1L, username, "super_admin", "super-admin");
        tokenStorage.removeToken(userInfo);
        log.info("超级管理员登出: username={}", username);
    }

    /**
     * 验证 Token
     */
    public boolean validateToken(String username, String token) {
        UserInfo userInfo = new UserInfo(1L, username, "super_admin", "super-admin");
        return tokenStorage.validateTokenInRedis(userInfo, token);
    }

    /**
     * 获取管理员信息
     */
    public SuperAdminConfig.Account getAccount(String username) {
        return config.findByUsername(username);
    }
}