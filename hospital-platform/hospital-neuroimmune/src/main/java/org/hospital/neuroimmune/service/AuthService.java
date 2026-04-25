package org.hospital.neuroimmune.service;

import org.hospital.common.model.LoginRequest;
import org.hospital.neuroimmune.model.LoginResult;

/**
 * 认证服务接口
 * 处理登录、角色识别、Token生成等认证相关逻辑
 */
public interface AuthService {

    /**
     * 统一登录方法
     * 自动识别角色并完成认证
     *
     * @param request 登录请求
     * @return 登录结果
     */
    LoginResult login(LoginRequest request);

    /**
     * 更新密码并移除Token
     *
     * @param userId 用户ID
     * @param role 用户角色 (admin, doctor, patient)
     * @param newPassword 新密码
     */
    void updatePasswordAndRemoveToken(Long userId, String role, String newPassword);
}