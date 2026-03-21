package org.hospital.neuroimmune.dto;

import lombok.Data;
import java.util.List;

/**
 * 分页响应
 */
@Data
public class PageResult<T> {
    private List<T> list;
    private Long total;
    private Integer pageNum;
    private Integer pageSize;

    public PageResult(List<T> list, Long total, Integer pageNum, Integer pageSize) {
        this.list = list;
        this.total = total;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
    }
}