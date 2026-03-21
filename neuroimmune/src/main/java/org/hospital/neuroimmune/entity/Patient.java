package org.hospital.neuroimmune.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

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
    private Integer age;
    private String phone;
    private String password;
    private String avatar;
    private String idCard;
    private Boolean hasFollowUp;
    private Boolean isRealAuth;
    private Long doctorId;
    private String doctorName;

    // 疾病分类
    private String diseaseType;  // MS, NMOSD, MG, MOGAD, 自身免疫性脑炎, GBS, CIDP, 其它疾病

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}