package org.hospital.neuroimmune.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 患者实体
 */
@Data
@TableName("patient")
public class Patient {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String gender;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime birthDate;  // 出生日期

    @TableField(exist = false)
    private Integer age;  // 计算字段，不持久化

    private String phone;
    private String password;
    private String avatar;
    private String idCard;
    @TableField(exist = false)
    private Boolean hasFollowUp;  // 计算字段，从 follow_up 表统计
    private Boolean isRealAuth;

    @TableField(exist = false)
    private Long doctorId;  // 非持久化字段，通过 patient_doctor_relation 查询获取

    @TableField(exist = false)
    private String doctorName;  // 非持久化字段，通过 patient_doctor_relation 查询获取

    @TableField(exist = false)
    private List<String> diseaseTypes;  // 从 patient_disease 表查询

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;     // 0-有效, 1-无效

    /**
     * 根据出生日期计算年龄
     */
    public Integer getAge() {
        if (birthDate == null) return null;
        LocalDateTime today = LocalDateTime.now();
        int calculatedAge = today.getYear() - birthDate.getYear();
        if (today.getMonthValue() < birthDate.getMonthValue() ||
            (today.getMonthValue() == birthDate.getMonthValue() && today.getDayOfMonth() < birthDate.getDayOfMonth())) {
            calculatedAge--;
        }
        return calculatedAge;
    }
}