package org.hospital.neuroimmune.dto;

import lombok.Data;

/**
 * 分页请求参数
 */
@Data
public class PageRequest {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private String keyword;
    private String status;
    private String gender;
    private Boolean isRealAuth;
    private Long doctorId;
    private Long patientId;
    private String type;
    private String department;
    private String startDate;
    private String endDate;

    /**
     * 计算偏移量，用于MySQL分页
     */
    public Integer getOffset() {
        return (pageNum - 1) * pageSize;
    }
}