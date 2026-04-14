package org.hospital.qmg.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 患者实体类
 */
@Data
public class Patient {
    /** 患者ID */
    private Integer id;
    /** 姓名 */
    private String name;
    /** 性别：'male' | 'female' */
    private String gender;
    /** 住院号 */
    private String admissionNumber;
    /** 手机号（必填） */
    private String phone;
    /** 创建时间 */
    private LocalDateTime createTime;
    /** 更新时间 */
    private LocalDateTime updateTime;
}
