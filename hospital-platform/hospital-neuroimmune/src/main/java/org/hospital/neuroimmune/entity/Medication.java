package org.hospital.neuroimmune.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.hospital.common.enums.RecordStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用药记录实体
 * 已重构：添加领域行为方法
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

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime date;

    private BigDecimal dosageValue;
    private String dosageUnit;
    private String frequency;
    private String route;
    private String duration;  // 服用时间段，如 "1个月"、"3个月"、"7天"

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endDate;  // 结束日期，根据 duration 自动计算

    private String notes;
    private Integer status;      // 0-进行中, 1-完成, 2-取消

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

    /**
     * 获取完整剂量描述
     */
    public String getFullDosage() {
        if (dosageValue == null || dosageUnit == null) return null;
        return dosageValue + dosageUnit;
    }
}