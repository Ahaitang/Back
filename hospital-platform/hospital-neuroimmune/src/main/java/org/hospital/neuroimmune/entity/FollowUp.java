package org.hospital.neuroimmune.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.hospital.common.enums.RecordStatus;

import java.time.LocalDateTime;

/**
 * 随访记录实体
 * 已重构：添加领域行为方法
 */
@Data
@TableName("follow_up")
public class FollowUp {
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
    private Integer status;      // 0-进行中, 1-完成, 2-取消
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

    // ===== 领域行为方法 =====

    /**
     * 判断是否进行中
     */
    public boolean isOngoing() {
        return RecordStatus.ONGOING.getCode() == (status != null ? status : 0);
    }

    /**
     * 判断是否已完成
     */
    public boolean isCompleted() {
        return RecordStatus.COMPLETED.getCode() == (status != null ? status : 0);
    }

    /**
     * 判断是否已取消
     */
    public boolean isCancelled() {
        return RecordStatus.CANCELLED.getCode() == (status != null ? status : 0);
    }

    /**
     * 标记为进行中
     */
    public void markOngoing() {
        this.status = RecordStatus.ONGOING.getCode();
    }

    /**
     * 标记为已完成
     */
    public void markCompleted() {
        this.status = RecordStatus.COMPLETED.getCode();
    }

    /**
     * 标记为已取消
     */
    public void markCancelled() {
        this.status = RecordStatus.CANCELLED.getCode();
    }

    /**
     * 获取状态枚举
     */
    public RecordStatus getRecordStatus() {
        return RecordStatus.fromCode(status != null ? status : 0);
    }
}