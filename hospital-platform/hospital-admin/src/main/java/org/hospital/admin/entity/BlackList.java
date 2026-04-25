package org.hospital.admin.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 黑名单实体
 */
@Data
@TableName("black_list")
public class BlackList {
    private Long id;
    private Long userId;
    private String role;
    private String module;
    private String reason;
    private LocalDateTime banTime;
    private LocalDateTime expireTime;
    private String status;
    private Long operatorId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}