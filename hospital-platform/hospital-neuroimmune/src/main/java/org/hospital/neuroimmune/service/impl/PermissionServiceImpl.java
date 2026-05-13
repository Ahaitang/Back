package org.hospital.neuroimmune.service.impl;

import org.hospital.neuroimmune.entity.Doctor;
import org.hospital.neuroimmune.entity.PatientDoctorRelation;
import org.hospital.neuroimmune.service.DoctorService;
import org.hospital.neuroimmune.service.DoctorRoleService;
import org.hospital.neuroimmune.service.PatientDoctorRelationService;
import org.hospital.neuroimmune.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * 权限服务实现
 */
@Service
public class PermissionServiceImpl implements PermissionService {

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private DoctorRoleService doctorRoleService;

    @Autowired
    private PatientDoctorRelationService relationService;

    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_DOCTOR = "DOCTOR";

    @Override
    public boolean isAdmin(Long userId) {
        if (userId == null) return false;
        List<String> roles = getUserRoles(userId);
        return roles != null && roles.contains(ROLE_ADMIN);
    }

    @Override
    public boolean isDoctor(Long userId) {
        if (userId == null) return false;
        List<String> roles = getUserRoles(userId);
        return roles != null && roles.contains(ROLE_DOCTOR);
    }

    @Override
    public List<String> getUserRoles(Long userId) {
        if (userId == null) return Collections.emptyList();
        return doctorRoleService.getRoleCodesByDoctorId(userId);
    }

    @Override
    public String checkRoleUpdatePermission(Long currentUserId, Long targetUserId, List<String> newRoles) {
        if (currentUserId == null) {
            return "用户信息获取失败";
        }

        Doctor currentUser = doctorService.getById(currentUserId);
        if (currentUser == null) {
            return "用户信息获取失败";
        }

        Doctor targetUser = doctorService.getById(targetUserId);
        if (targetUser == null) {
            return "用户不存在";
        }

        List<String> currentUserRoles = getUserRoles(currentUserId);
        boolean currentUserIsAdmin = currentUserRoles.contains(ROLE_ADMIN);

        // 修改自己的角色
        if (targetUserId.equals(currentUserId)) {
            // 非管理员不能把自己设置为管理员
            if (!currentUserIsAdmin && newRoles.contains(ROLE_ADMIN)) {
                return "非管理员不能将自己设置为管理员";
            }
            // 管理员不能移除自己的管理员角色（防止意外锁死）
            if (currentUserIsAdmin && !newRoles.contains(ROLE_ADMIN)) {
                return "管理员不能移除自己的管理员角色";
            }
            // 可以修改自己的其他角色（同级修改）
            return null; // 允许
        }

        // 修改别人的角色 - 必须是管理员
        if (!currentUserIsAdmin) {
            return "只有管理员可以修改其他用户的角色";
        }

        // 管理员不能修改同级或更高等级的用户
        Integer currentLevel = currentUser.getLevel();
        Integer targetLevel = targetUser.getLevel();

        // level 数字越小权限越高，null 表示普通医生
        if (currentLevel == null) currentLevel = 999;
        if (targetLevel == null) targetLevel = 999;

        if (currentLevel >= targetLevel) {
            return "无权限修改同级或更高等级用户";
        }

        return null; // 允许
    }

    @Override
    public void updateUserRolesAndLevel(Long userId, List<String> roles, Integer level) {
        if (roles != null) {
            doctorRoleService.setDoctorRoles(userId, roles);
        }
        if (level != null) {
            doctorService.updateRoleAndLevel(userId, null, level);
        }
    }

    @Override
    public boolean canAccessData(Long userId, String role, Long targetPatientId) {
        if (userId == null) return false;

        // 管理员可以访问所有数据
        if (isAdmin(userId)) return true;

        // 医生只能访问自己的患者数据
        if (ROLE_DOCTOR.equalsIgnoreCase(role)) {
            if (targetPatientId == null) return true; // 列表查询时由 Service 层过滤
            List<PatientDoctorRelation> relations = relationService.getActivePatientsByDoctor(userId);
            return relations.stream().anyMatch(r -> r.getPatientId().equals(targetPatientId));
        }

        // 患者只能访问自己的数据
        if ("patient".equalsIgnoreCase(role)) {
            return targetPatientId == null || targetPatientId.equals(userId);
        }

        return false;
    }

    @Override
    public String checkPatientAccessPermission(Long userId, String role, Long patientId) {
        if (userId == null) {
            return "用户信息获取失败";
        }
        if (patientId == null) {
            return "记录不存在";
        }

        // 患者只能查看自己的信息
        if ("patient".equalsIgnoreCase(role) && !userId.equals(patientId)) {
            return "无权查看其他患者信息";
        }

        if ("doctor".equalsIgnoreCase(role)) {
            List<PatientDoctorRelation> relations = relationService.getActivePatientsByDoctor(userId);
            boolean hasAccess = relations.stream().anyMatch(r -> r.getPatientId().equals(patientId));
            return hasAccess ? null : "无权查看未绑定患者信息";
        }

        if ("admin".equalsIgnoreCase(role)) {
            return null;
        }

        return "无权访问该患者信息";
    }
}
