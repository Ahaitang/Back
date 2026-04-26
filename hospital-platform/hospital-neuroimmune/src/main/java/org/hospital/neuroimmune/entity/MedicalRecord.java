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
 * 病历记录实体
 * 继承 BaseRecordEntity，复用状态管理行为
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("medical_record")
public class MedicalRecord extends BaseRecordEntity {
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

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime date;

    private String content;
    private String attachments;
    private String notes;       // 备注

    private Long relatedEpisodeId;   // 关联的发作记录ID

    @TableField(exist = false)
    private Integer relatedEpisodeNumber; // 关联的发作次数（第几次）

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;     // 0-有效, 1-无效
}