package org.hospital.neuroimmune.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用药记录实体
 */
@Data
@TableName("medication")
public class Medication {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long patientId;
    private String patientName;
    private Long doctorId;
    private String doctorName;
    private String medicationName;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime date;

    private String dosage;
    private String unit;
    private String frequency;
    private String route;
    private String duration;  // 服用时间段，如 "1个月"、"3个月"、"7天"

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime endDate;  // 结束日期，根据 duration 自动计算

    private String notes;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}