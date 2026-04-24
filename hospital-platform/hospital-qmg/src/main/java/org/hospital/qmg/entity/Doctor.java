package org.hospital.qmg.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 医生实体类
 */
@Data
public class Doctor {
    /** 医生ID */
    private Integer id;
    /** 工号（唯一标识） */
    private String employeeNumber;
    /** 用户名 */
    private String username;
    /** 密码（加密后） */
    private String password;
    /** 权限等级：0=超级管理员，1=管理员，2=普通医生 */
    private Integer level;
    /** 创建时间 */
    private LocalDateTime createTime;
    /** 更新时间 */
    private LocalDateTime updateTime;
    /** 是否删除：0-有效，1-无效 */
    private Integer isDeleted;
}
