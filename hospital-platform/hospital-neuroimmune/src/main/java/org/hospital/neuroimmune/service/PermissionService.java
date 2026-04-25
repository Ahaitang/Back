package org.hospital.neuroimmune.service;

import org.hospital.neuroimmune.entity.Doctor;
import java.util.List;

/**
 * 权限服务
 * 处理角色和权限相关的业务逻辑
 */
public interface PermissionService {

    /**
     * 检查用户是否是管理员
     * @param userId 用户ID
     * @return 是否是管理员
     */
    boolean isAdmin(Long userId);

    /**
     * 检查用户是否是医生
     * @param userId 用户ID
     * @return 是否是医生
     */
    boolean isDoctor(Long userId);

    /**
     * 获取用户角色列表
     * @param userId 用户ID
     * @return 角色代码列表
     */
    List<String> getUserRoles(Long userId);

    /**
     * 检查是否可以修改用户角色
     * @param currentUserId 当前用户ID
     * @param targetUserId 目标用户ID
     * @param newRoles 新角色列表
     * @return 检查结果（成功返回 null，失败返回错误信息）
     */
    String checkRoleUpdatePermission(Long currentUserId, Long targetUserId, List<String> newRoles);

    /**
     * 更新用户角色和等级
     * @param userId 用户ID
     * @param roles 新角色列表
     * @param level 新等级
     */
    void updateUserRolesAndLevel(Long userId, List<String> roles, Integer level);

    /**
     * 检查数据访问权限
     * @param userId 当前用户ID
     * @param role 用户角色
     * @param targetPatientId 目标患者ID（可选）
     * @return 是否有权限访问
     */
    boolean canAccessData(Long userId, String role, Long targetPatientId);

    /**
     * 检查患者数据访问权限
     * @param userId 当前用户ID
     * @param role 用户角色
     * @param patientId 目标患者ID
     * @return 检查结果（成功返回 null，失败返回错误信息）
     */
    String checkPatientAccessPermission(Long userId, String role, Long patientId);
}