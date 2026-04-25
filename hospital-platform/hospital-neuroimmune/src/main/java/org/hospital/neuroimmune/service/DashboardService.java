package org.hospital.neuroimmune.service;

import java.util.Map;

/**
 * 仪表盘服务
 * 处理仪表盘数据聚合逻辑
 */
public interface DashboardService {

    /**
     * 获取管理员统计数据
     * @return 统计数据Map
     */
    Map<String, Object> getAdminStats();

    /**
     * 获取医生统计数据
     * @param doctorId 医生ID
     * @return 统计数据Map
     */
    Map<String, Object> getDoctorStats(Long doctorId);

    /**
     * 获取患者统计数据
     * @param patientId 患者ID
     * @return 统计数据Map
     */
    Map<String, Object> getPatientStats(Long patientId);

    /**
     * 根据用户角色获取统计数据
     * @param userId 用户ID
     * @param role 用户角色
     * @return 统计数据Map
     */
    Map<String, Object> getStatsByRole(Long userId, String role);
}