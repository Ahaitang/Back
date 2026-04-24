package org.hospital.neuroimmune.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 疾病发作记录实体
 */
@Data
@TableName("disease_episode")
public class DiseaseEpisode {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long patientId;
    @TableField(exist = false)
    private String patientName;

    private Integer episodeNumber;     // 发作次数（第几次发作）

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime episodeDate; // 发作时间

    private String chiefComplaint;     // 主诉
    private String symptoms;           // 症状
    private String diseaseProgress;    // 病情变化过程
    private String treatmentProcess;   // 诊治经过
    private String diagnosis;          // 诊断结果

    private String hospital;           // 就诊医院
    private String department;         // 科室

    private String notes;              // 备注

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    private Integer isDeleted;     // 0-有效, 1-无效

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}