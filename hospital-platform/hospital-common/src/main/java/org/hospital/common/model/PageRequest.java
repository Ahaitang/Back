package org.hospital.common.model;

import lombok.Data;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

/**
 * 分页请求参数
 */
@Data
public class PageRequest {

    @Min(value = 1, message = "页码最小为1")
    private Integer pageNum = 1;

    @Min(value = 1, message = "每页条数最小为1")
    @Max(value = 100, message = "每页条数最大为100")
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
     * 计算偏移量，用于 MySQL 分页
     */
    public Integer getOffset() {
        return (pageNum - 1) * pageSize;
    }
}