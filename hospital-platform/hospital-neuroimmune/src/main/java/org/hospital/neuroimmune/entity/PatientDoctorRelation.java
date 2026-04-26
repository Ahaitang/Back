package org.hospital.neuroimmune.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 患者-医生绑定关系实体
 */
@Data
@TableName("patient_doctor_relation")
public class PatientDoctorRelation {
    // 状态常量（用于解绑逻辑）
    public static final int STATUS_ACTIVE = 1;
    public static final int STATUS_INACTIVE = 0;

    // 绑定状态常量（用于审核流程）
    public static final Integer BIND_STATUS_PENDING = 0;     // 待确认
    public static final Integer BIND_STATUS_CONFIRMED = 1;   // 已确认
    public static final Integer BIND_STATUS_REJECTED = 2;    // 已拒绝

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long patientId;
    @TableField(exist = false)
    private String patientName;

    private Long doctorId;
    @TableField(exist = false)
    private String doctorName;

    /**
     * 关系类型：primary-主治医生, consultant-会诊医生
     */
    private String relationType;

    /**
     * 绑定状态：1-生效中, 0-已解绑
     */
    private Integer status;

    /**
     * 绑定审核状态：0-待确认, 1-已确认, 2-已拒绝
     */
    private Integer bindStatus;

    /**
     * 绑定方式：system-系统分配, patient-患者选择, doctor-医生邀请
     */
    private String bindMethod;

    private String remark;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime requestTime;  // 绑定请求时间

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime confirmTime;  // 确认/拒绝时间

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime bindTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime unbindTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;     // 0-有效, 1-无效
}