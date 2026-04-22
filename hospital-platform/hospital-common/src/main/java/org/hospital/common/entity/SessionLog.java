package org.hospital.common.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 会话审计日志实体
 */
@Data
@TableName("session_log")
public class SessionLog {
    private Long id;
    private Long userId;
    private String role;
    private String module;
    private LocalDateTime loginTime;
    private LocalDateTime logoutTime;
    private String ip;
    private String userAgent;
    private String device;
    private Integer durationSeconds;
    private String operationType;
    private Long operatorId;
    private LocalDateTime createTime;
}