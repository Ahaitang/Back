package org.hospital.common.security;

import org.hospital.common.model.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * SecurityContext 辅助工具类
 * 用于从 Spring Security 上下文中获取当前用户信息
 */
public class SecurityContextHelper {

    private SecurityContextHelper() {
        // 工具类，禁止实例化
    }

    /**
     * 获取当前登录用户信息
     *
     * @return 当前用户信息，未登录返回 null
     */
    public static UserInfo getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserInfo userInfo) {
            return userInfo;
        }
        return null;
    }

    /**
     * 获取当前用户ID
     *
     * @return 用户ID，未登录返回 null
     */
    public static Long getCurrentUserId() {
        UserInfo user = getCurrentUser();
        return user != null ? user.getUserId() : null;
    }

    /**
     * 获取当前用户角色
     *
     * @return 角色名称（admin/doctor/patient），未登录返回 null
     */
    public static String getCurrentRole() {
        UserInfo user = getCurrentUser();
        return user != null ? user.getRole() : null;
    }

    /**
     * 判断当前用户是否为医生
     *
     * @return 是否为医生角色
     */
    public static boolean isDoctor() {
        return "doctor".equals(getCurrentRole());
    }

    /**
     * 判断当前用户是否为患者
     *
     * @return 是否为患者角色
     */
    public static boolean isPatient() {
        return "patient".equals(getCurrentRole());
    }

    /**
     * 判断当前用户是否为管理员
     *
     * @return 是否为管理员角色
     */
    public static boolean isAdmin() {
        return "admin".equals(getCurrentRole());
    }

    /**
     * 应用数据权限范围到分页请求
     * 医生角色：只能查看自己相关的数据
     * 患者角色：只能查看自己的数据
     *
     * @param request 分页请求对象
     */
    public static void applyDataScope(PageRequest request) {
        UserInfo user = getCurrentUser();
        if (user == null || request == null) {
            return;
        }
        if ("doctor".equals(user.getRole())) {
            request.setDoctorId(user.getUserId());
        } else if ("patient".equals(user.getRole())) {
            request.setPatientId(user.getUserId());
        }
    }

    /**
     * 获取当前医生ID（仅医生角色有效）
     *
     * @return 医生ID，非医生角色返回 null
     */
    public static Long getCurrentDoctorId() {
        return isDoctor() ? getCurrentUserId() : null;
    }

    /**
     * 获取当前患者ID（仅患者角色有效）
     *
     * @return 患者ID，非患者角色返回 null
     */
    public static Long getCurrentPatientId() {
        return isPatient() ? getCurrentUserId() : null;
    }
}