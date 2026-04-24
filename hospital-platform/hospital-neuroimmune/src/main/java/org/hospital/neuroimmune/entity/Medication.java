package org.hospital.neuroimmune.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
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
    @TableField(exist = false)
    private String patientName;
    private Long doctorId;
    @TableField(exist = false)
    private String doctorName;
    private String medicationName;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    private BigDecimal dosageValue;
    private String dosageUnit;
    private String frequency;
    private String route;
    private String duration;  // 服用时间段，如 "1个月"、"3个月"、"7天"

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;  // 结束日期，根据 duration 自动计算

    private String notes;
    private Integer status;      // 0-进行中, 1-完成, 2-取消

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;     // 0-有效, 1-无效
}