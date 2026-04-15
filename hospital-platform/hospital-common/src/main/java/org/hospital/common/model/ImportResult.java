package org.hospital.common.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.ArrayList;
import java.util.List;

/**
 * 导入结果封装
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImportResult {
    /** 总行数 */
    private int total;
    /** 成功条数 */
    private int success;
    /** 失败条数 */
    private int failed;
    /** 失败明细 */
    private List<RowError> errors = new ArrayList<>();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RowError {
        /** 行号（从 1 开始） */
        private int row;
        /** 失败原因 */
        private String message;
    }

    /**
     * 添加错误信息
     */
    public void addError(int row, String message) {
        this.errors.add(new RowError(row, message));
        this.failed++;
    }

    /**
     * 记录成功
     */
    public void success() {
        this.success++;
    }

    /**
     * 设置总行数
     */
    public void setTotal(int total) {
        this.total = total;
    }
}