package org.hospital.qmg.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码生成工具类
 * 用于生成BCrypt加密后的密码
 * 
 * 使用方法：
 * 1. 运行此类的main方法
 * 2. 复制输出的BCrypt hash
 * 3. 在SQL中使用该hash更新密码
 */
public class PasswordGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // 生成密码123456的BCrypt hash
        String password = "123456";
        String encodedPassword = encoder.encode(password);
        
        System.out.println("==========================================");
        System.out.println("原始密码: " + password);
        System.out.println("BCrypt加密后: " + encodedPassword);
        System.out.println("==========================================");
        
        // 验证密码
        boolean matches = encoder.matches(password, encodedPassword);
        System.out.println("密码验证结果: " + matches);
        System.out.println("==========================================");
        System.out.println("SQL更新语句：");
        System.out.println("UPDATE `doctor` SET `password` = '" + encodedPassword + "' WHERE `username` = 'admin';");
        System.out.println("或者插入新账号：");
        System.out.println("INSERT INTO `doctor` (`username`, `password`, `role`) VALUES ('admin', '" + encodedPassword + "', 'admin');");
        System.out.println("==========================================");
    }
}
