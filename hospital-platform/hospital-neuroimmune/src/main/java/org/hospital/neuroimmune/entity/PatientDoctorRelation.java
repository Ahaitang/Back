package org.hospital.neuroimmune.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
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

    // 状态常量
    public static final int STATUS_ACTIVE = 1;
    public static final int STATUS_INACTIVE = 0;

    /**
     * 绑定方式：system-系统分配, patient-患者选择, doctor-医生邀请
     */
    private String bindMethod;

    private String remark;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime bindTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime unbindTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    private Integer isDeleted;     // 0-有效, 1-无效
}