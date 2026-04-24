package org.hospital.neuroimmune.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 随访记录实体
 */
@Data
@TableName("follow_up")
public class FollowUp {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long patientId;
    private String patientName;
    private String patientGender;
    private Integer patientAge;
    private Long doctorId;
    private String doctorName;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    private String project;
    private String type;
    private Integer status;      // 0-进行中, 1-完成, 2-取消
    private String content;

    // 详细字段
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime outpatientTime;       // 门诊时间
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate hospitalizationTime;  // 住院时间
    private String examinationItems;            // 检查项目
    private String hospital;                    // 医院
    private String department;                  // 科室
    private String notes;                       // 备注

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}