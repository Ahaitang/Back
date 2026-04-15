package org.hospital.common.security;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;

/**
 * 用户身份信息
 * 存储在 JWT Token 和 Redis 中
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 用户ID */
    private Long userId;

    /** 用户名 */
    private String username;

    /** 角色：admin, doctor, patient */
    private String role;

    /** 所属模块：qmg, neuroimmune */
    private String module;

    /**
     * 生成 Redis 存储的 Key
     * 格式：token:{module}:{role}:{userId}
     */
    public String getRedisKey() {
        return String.format("token:%s:%s:%d", module, role, userId);
    }

    /**
     * 生成用户唯一标识
     */
    public String getUniqueId() {
        return String.format("%s:%s:%d", module, role, userId);
    }
}