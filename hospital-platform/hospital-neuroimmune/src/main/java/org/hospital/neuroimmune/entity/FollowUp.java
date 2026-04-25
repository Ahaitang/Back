package org.hospital.neuroimmune.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hospital.common.model.BaseRecordEntity;

import java.time.LocalDateTime;

/**
 * 随访记录实体
 * 继承 BaseRecordEntity，复用状态管理行为
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("follow_up")
public class FollowUp extends BaseRecordEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long patientId;
    @TableField(exist = false)
    private String patientName;
    @TableField(exist = false)
    private String patientGender;
    @TableField(exist = false)
    private Integer patientAge;
    private Long doctorId;
    @TableField(exist = false)
    private String doctorName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime date;

    private String project;
    private String type;
    private String content;

    // 详细字段
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime outpatientTime;       // 门诊时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime hospitalizationTime;  // 住院时间
    private String examinationItems;            // 检查项目
    private String hospital;                    // 医院
    private String department;                  // 科室
    private String notes;                       // 备注

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;     // 0-有效, 1-无效
}