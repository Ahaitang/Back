package org.hospital.admin.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.hospital.common.util.CredentialFileWriter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.List;

/**
 * 超级管理员配置
 * 从 application.yaml 读取超级管理员账号列表
 */
@Data
@Slf4j
@Component
@ConfigurationProperties(prefix = "super-admin")
public class SuperAdminConfig {

    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int PASSWORD_LENGTH = 16;

    private List<Account> accounts;

    @Data
    public static class Account {
        private String username;
        private String password;
        private String name;
    }

    @PostConstruct
    public void init() {
        if (accounts == null || accounts.isEmpty()) {
            return;
        }
        boolean generated = false;
        for (Account account : accounts) {
            if (account.getPassword() == null || account.getPassword().isBlank()) {
                String randomPassword = generateRandomPassword();
                account.setPassword(randomPassword);
                generated = true;
                log.warn("超级管理员 [{}] 未设置密码，已自动生成随机密码", account.getUsername());
            }
        }
        if (generated) {
            for (Account account : accounts) {
                CredentialFileWriter.writeCredential("超级管理员", account.getUsername(), account.getPassword());
            }
        }
    }

    private String generateRandomPassword() {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(PASSWORD_LENGTH);
        for (int i = 0; i < PASSWORD_LENGTH; i++) {
            sb.append(CHARS.charAt(random.nextInt(CHARS.length())));
        }
        return sb.toString();
    }

    /**
     * 根据用户名查找账号
     */
    public Account findByUsername(String username) {
        if (accounts == null || username == null) {
            return null;
        }
        return accounts.stream()
            .filter(a -> a.getUsername().equals(username))
            .findFirst()
            .orElse(null);
    }

    /**
     * 验证账号密码
     */
    public boolean validate(String username, String password) {
        Account account = findByUsername(username);
        if (account == null) {
            return false;
        }
        return MessageDigest.isEqual(
            account.getPassword().getBytes(java.nio.charset.StandardCharsets.UTF_8),
            password.getBytes(java.nio.charset.StandardCharsets.UTF_8)
        );
    }
}