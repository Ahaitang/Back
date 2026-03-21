package org.hospital.neuroimmune.dto;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

/**
 * 导入结果
 */
@Data
public class ImportResult {
    private int total;          // 总行数
    private int success;        // 成功数
    private int failed;         // 失败数
    private List<String> errors = new ArrayList<>();  // 错误信息

    public void addError(int row, String message) {
        errors.add("第" + row + "行: " + message);
        failed++;
    }

    public void success() {
        success++;
    }
}