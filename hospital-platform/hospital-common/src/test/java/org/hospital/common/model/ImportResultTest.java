package org.hospital.common.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.util.Arrays;
import static org.junit.jupiter.api.Assertions.*;

/**
 * ImportResult 导入结果测试
 */
class ImportResultTest {

    @Test
    @DisplayName("测试创建默认导入结果")
    void testDefaultImportResult() {
        ImportResult result = new ImportResult();

        assertEquals(0, result.getTotal());
        assertEquals(0, result.getSuccess());
        assertEquals(0, result.getFailed());
        assertTrue(result.getErrors().isEmpty());
    }

    @Test
    @DisplayName("测试添加错误")
    void testAddError() {
        ImportResult result = new ImportResult();
        result.setTotal(10);

        result.addError(1, "姓名为空");
        result.addError(2, "手机号格式错误");

        assertEquals(2, result.getFailed());
        assertEquals(2, result.getErrors().size());

        ImportResult.RowError error1 = result.getErrors().get(0);
        assertEquals(1, error1.getRow());
        assertEquals("姓名为空", error1.getMessage());
    }

    @Test
    @DisplayName("测试记录成功")
    void testSuccess() {
        ImportResult result = new ImportResult();
        result.setTotal(10);

        result.success();
        result.success();
        result.success();

        assertEquals(3, result.getSuccess());
    }

    @Test
    @DisplayName("测试混合成功和失败")
    void testMixedSuccessAndError() {
        ImportResult result = new ImportResult();
        result.setTotal(10);

        result.success();
        result.success();
        result.addError(3, "数据格式错误");
        result.success();

        assertEquals(3, result.getSuccess());
        assertEquals(1, result.getFailed());
        assertEquals(1, result.getErrors().size());
    }

    @Test
    @DisplayName("测试 RowError 创建")
    void testRowError() {
        ImportResult.RowError error = new ImportResult.RowError(5, "必填字段为空");

        assertEquals(5, error.getRow());
        assertEquals("必填字段为空", error.getMessage());
    }
}