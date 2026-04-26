package org.hospital.neuroimmune.service;

import org.hospital.common.model.LoginRequest;
import org.hospital.neuroimmune.model.LoginResult;
import org.hospital.neuroimmune.model.RegisterRequest;
import org.hospital.neuroimmune.model.RegisterResult;

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

    /**
     * 患者注册
     * 创建患者账户并建立待确认的医生绑定关系
     *
     * @param request 注册请求
     * @return 注册结果，包含患者ID和状态
     */
    RegisterResult register(RegisterRequest request);

    /**
     * 检查手机号是否已注册
     *
     * @param phone 手机号
     * @return true表示已存在
     */
    boolean checkPhoneExists(String phone);
}