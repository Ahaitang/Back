package org.hospital.common.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * PageRequest 分页请求测试
 */
class PageRequestTest {

    @Test
    @DisplayName("测试创建默认分页请求")
    void testDefaultPageRequest() {
        PageRequest request = new PageRequest();

        assertEquals(1, request.getPageNum());
        assertEquals(10, request.getPageSize());
        assertNull(request.getKeyword());
    }

    @Test
    @DisplayName("测试创建带参数的分页请求")
    void testPageRequestWithParams() {
        PageRequest request = new PageRequest();
        request.setPageNum(2);
        request.setPageSize(20);
        request.setKeyword("张三");

        assertEquals(2, request.getPageNum());
        assertEquals(20, request.getPageSize());
        assertEquals("张三", request.getKeyword());
    }

    @Test
    @DisplayName("测试分页参数边界 - 第一页")
    void testFirstPage() {
        PageRequest request = new PageRequest();
        request.setPageNum(1);
        request.setPageSize(10);

        assertEquals(1, request.getPageNum());
    }

    @Test
    @DisplayName("测试分页参数边界 - 大页数")
    void testLargePage() {
        PageRequest request = new PageRequest();
        request.setPageNum(1000);
        request.setPageSize(50);

        assertEquals(1000, request.getPageNum());
        assertEquals(50, request.getPageSize());
    }
}