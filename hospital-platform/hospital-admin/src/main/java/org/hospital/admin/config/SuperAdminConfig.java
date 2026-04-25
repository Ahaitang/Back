package org.hospital.admin.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import java.util.List;

/**
 * 超级管理员配置
 * 从 application.yaml 读取超级管理员账号列表
 */
@Data
@Component
@ConfigurationProperties(prefix = "super-admin")
public class SuperAdminConfig {

    private List<Account> accounts;

    @Data
    public static class Account {
        private String username;
        private String password;
        private String name;
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
        return account.getPassword().equals(password);
    }
}