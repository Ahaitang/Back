package org.hospital.common.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Result 响应封装测试
 */
class ResultTest {

    @Test
    @DisplayName("测试 Neuroimmune 格式成功响应")
    void testSuccess() {
        Result<String> result = Result.success("data");

        assertEquals(200, result.getCode());
        assertEquals("success", result.getMessage());
        assertEquals("data", result.getData());
    }

    @Test
    @DisplayName("测试 Neuroimmune 格式成功响应 - 无数据")
    void testSuccessWithoutData() {
        Result<Void> result = Result.success();

        assertEquals(200, result.getCode());
        assertEquals("success", result.getMessage());
        assertNull(result.getData());
    }

    @Test
    @DisplayName("测试 Neuroimmune 格式错误响应")
    void testError() {
        Result<Void> result = Result.error("操作失败");

        assertEquals(500, result.getCode());
        assertEquals("操作失败", result.getMessage());
        assertNull(result.getData());
    }

    @Test
    @DisplayName("测试 Neuroimmune 格式错误响应 - 自定义code")
    void testErrorWithCode() {
        Result<Void> result = Result.error(400, "参数错误");

        assertEquals(400, result.getCode());
        assertEquals("参数错误", result.getMessage());
    }

    @Test
    @DisplayName("测试 QMG 格式成功响应")
    void testQmgSuccess() {
        Result<String> result = Result.qmgSuccess("data");

        assertEquals(1, result.getCode());
        assertEquals("success", result.getMessage());
        assertEquals("data", result.getData());
    }

    @Test
    @DisplayName("测试 QMG 格式成功响应 - 无数据")
    void testQmgSuccessWithoutData() {
        Result<Void> result = Result.qmgSuccess();

        assertEquals(1, result.getCode());
        assertEquals("success", result.getMessage());
    }

    @Test
    @DisplayName("测试 QMG 格式错误响应")
    void testQmgError() {
        Result<Void> result = Result.qmgError("患者不存在");

        assertEquals(0, result.getCode());
        assertEquals("患者不存在", result.getMessage());
    }
}