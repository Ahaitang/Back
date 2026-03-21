package org.hospital.qmg.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 批量导入结果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BatchImportResult {
    /** 成功条数 */
    private int successCount;
    /** 失败条数 */
    private int failCount;
    /** 失败明细：行号(从1开始) -> 原因 */
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

    public void addError(int row, String message) {
        this.errors.add(new RowError(row, message));
    }
}
