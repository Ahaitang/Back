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
    private String duration;
    private String notes;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}