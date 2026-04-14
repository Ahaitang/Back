package org.hospital.qmg.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 患者-医生对应实体类
 */
@Data
public class PatientDoctor {
    /** 主键ID */
    private Integer id;
    /** 患者ID */
    private Integer patientId;
    /** 医生ID */
    private Integer doctorId;
    /** 创建时间 */
    private LocalDateTime createTime;
    /** 更新时间 */
    private LocalDateTime updateTime;
}
