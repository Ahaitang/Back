package org.hospital.common.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码加密工具类
 */
public class PasswordUtil {

    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    /**
     * 加密密码
     */
    public static String encode(String rawPassword) {
        return encoder.encode(rawPassword);
    }

    /**
     * 验证密码
     */
    public static boolean matches(String rawPassword, String encodedPassword) {
        return encoder.matches(rawPassword, encodedPassword);
    }

    /**
     * 获取加密器实例
     */
    public static BCryptPasswordEncoder getEncoder() {
        return encoder;
    }

    /**
     * 命令行工具：生成 BCrypt 加密密码
     * 使用方法：java -cp hospital-common.jar org.hospital.common.util.PasswordUtil 123456
     */
    public static void main(String[] args) {
        String password = args.length > 0 ? args[0] : "123456";
        String encodedPassword = encode(password);

        System.out.println("==========================================");
        System.out.println("原始密码: " + password);
        System.out.println("BCrypt加密后: " + encodedPassword);
        System.out.println("==========================================");
        System.out.println("验证结果: " + matches(password, encodedPassword));
        System.out.println("==========================================");
    }
}