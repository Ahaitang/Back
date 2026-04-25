package org.hospital.admin.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 踢用户下线请求 DTO
 */
@Data
public class KickUserRequest {
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotNull(message = "角色不能为空")
    private String role;

    @NotNull(message = "模块不能为空")
    private String module;

    private Boolean addToBlacklist;

    private Integer banHours;
}