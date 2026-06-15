package org.hospital.admin.model;

import lombok.Data;

import java.util.List;

/**
 * 日志读取响应
 */
@Data
public class LogReadResponse {
    private List<String> lines;
    private Long totalLines;
    private Integer currentPage;
    private Boolean hasMore;
}
