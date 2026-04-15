package org.hospital.common.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * PasswordUtil 工具类测试
 */
class PasswordUtilTest {

    @Test
    @DisplayName("测试密码加密")
    void testEncode() {
        String rawPassword = "123456";
        String encoded = PasswordUtil.encode(rawPassword);

        assertNotNull(encoded);
        assertTrue(encoded.length() > 0);
        // BCrypt 加密后长度为 60
        assertEquals(60, encoded.length());
        // 加密结果每次不同（BCrypt 包含随机盐）
        String encoded2 = PasswordUtil.encode(rawPassword);
        assertNotEquals(encoded, encoded2);
    }

    @Test
    @DisplayName("测试密码匹配 - 正确密码")
    void testMatchesCorrectPassword() {
        String rawPassword = "123456";
        String encoded = PasswordUtil.encode(rawPassword);

        assertTrue(PasswordUtil.matches(rawPassword, encoded));
    }

    @Test
    @DisplayName("测试密码匹配 - 错误密码")
    void testMatchesWrongPassword() {
        String rawPassword = "123456";
        String encoded = PasswordUtil.encode(rawPassword);

        assertFalse(PasswordUtil.matches("wrong_password", encoded));
    }

    @Test
    @DisplayName("测试密码匹配 - 空密码")
    void testMatchesEmptyPassword() {
        String rawPassword = "";
        String encoded = PasswordUtil.encode(rawPassword);

        assertTrue(PasswordUtil.matches("", encoded));
        assertFalse(PasswordUtil.matches("123456", encoded));
    }

    @Test
    @DisplayName("测试获取加密器实例")
    void testGetEncoder() {
        assertNotNull(PasswordUtil.getEncoder());
    }
}