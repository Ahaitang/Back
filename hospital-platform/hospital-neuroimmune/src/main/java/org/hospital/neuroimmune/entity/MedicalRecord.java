package org.hospital.neuroimmune.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 病历记录实体
 */
@Data
@TableName("medical_record")
public class MedicalRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long patientId;
    private Long doctorId;
    @TableField(exist = false)
    private String patientName;
    private String type;        // 门诊病历, 住院病历, 外院病历
    private String diagnosis;
    private String hospital;
    private String department;
    @TableField(exist = false)
    private String doctorName;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    private String content;
    private String attachments;
    private String notes;       // 备注
    private Integer status;      // 0-进行中, 1-完成, 2-取消

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;     // 0-有效, 1-无效
}