package org.hospital.common.security;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 在线用户信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OnlineUser {

    /** 用户ID */
    private Long userId;

    /** 角色 */
    private String role;

    /** 所属模块 */
    private String module;

    /** Redis Key */
    private String redisKey;

    /** Token 剩余有效期（秒） */
    private Long ttlSeconds;
}