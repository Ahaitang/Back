package org.hospital.neuroimmune.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 医生实体（同时支持管理员角色）
 */
@Data
@TableName("doctor")
public class Doctor {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String title;
    private String department;
    private String hospital;
    private String phone;
    private String password;
    private String avatar;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @TableLogic
    private Integer isDeleted;     // 0-有效, 1-无效

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    private Integer level;         // 管理等级，1最高，null表示普通医生

    @TableField(exist = false)
    private List<String> roles;

    // 非数据库字段，查询时动态计算
    @TableField(exist = false)
    private Integer patientCount;
}