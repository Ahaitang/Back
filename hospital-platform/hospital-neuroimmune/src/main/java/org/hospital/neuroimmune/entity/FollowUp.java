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
 * 简化字段：保留患者、医生、周期性门诊时间、住院时间、检查项目、备注
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

    // 门诊随访周期字段
    private String outpatientCycleType;     // 周期类型：monthly/weekly/quarterly
    private String outpatientCycleValue;    // 周期值：每月几号/每周几/季度日期
    private String outpatientTimeSlot;      // 时间段：morning/afternoon/evening

    // 住院时间
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime hospitalizationTime;

    // 随访检查类型（字典ID）
    private Long followUpExamTypeId;
    @TableField(exist = false)
    private String followUpExamTypeName;    // 类型名称（从字典查询填充）

    // 检查项目（勾选结果，逗号分隔）
    private String examinationItems;

    // 备注
    private String notes;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;     // 0-有效, 1-无效
}