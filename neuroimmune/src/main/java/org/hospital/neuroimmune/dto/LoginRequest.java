package org.hospital.neuroimmune.dto;

import lombok.Data;

/**
 * 登录请求
 */
@Data
public class LoginRequest {
    private String username;
    private String password;
    // 角色: admin, doctor, patient
    private String role;
}