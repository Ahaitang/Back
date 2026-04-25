package org.hospital.neuroimmune.service.impl;

import org.hospital.common.model.PageRequest;
import org.hospital.neuroimmune.service.DashboardService;
import org.hospital.neuroimmune.service.PatientService;
import org.hospital.neuroimmune.service.DoctorService;
import org.hospital.neuroimmune.service.FollowUpService;
import org.hospital.neuroimmune.service.MedicationService;
import org.hospital.neuroimmune.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 仪表盘服务实现
 */
@Service("neuroimmuneDashboardService")
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private PatientService patientService;

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private FollowUpService followUpService;

    @Autowired
    private MedicationService medicationService;

    @Autowired
    private PermissionService permissionService;

    @Override
    public Map<String, Object> getAdminStats() {
        Map<String, Object> stats = new HashMap<>();
        PageRequest request = new PageRequest();
        request.setPageSize(1);

        stats.put("totalPatients", patientService.getList(request).getTotal());
        stats.put("totalDoctors", doctorService.getDoctorList(request).getTotal());
        stats.put("pendingFollowUps", followUpService.getList(request).getTotal());
        stats.put("totalMedications", medicationService.getList(request).getTotal());

        return stats;
    }

    @Override
    public Map<String, Object> getDoctorStats(Long doctorId) {
        Map<String, Object> stats = new HashMap<>();
        PageRequest request = new PageRequest();
        request.setPageSize(1);

        stats.put("totalPatients", patientService.getListByDoctorId(doctorId, request).getTotal());

        PageRequest doctorRequest = new PageRequest();
        doctorRequest.setPageSize(1);
        doctorRequest.setDoctorId(doctorId);
        stats.put("pendingFollowUps", followUpService.getList(doctorRequest).getTotal());
        stats.put("totalMedications", medicationService.getList(doctorRequest).getTotal());

        return stats;
    }

    @Override
    public Map<String, Object> getPatientStats(Long patientId) {
        Map<String, Object> stats = new HashMap<>();
        PageRequest request = new PageRequest();
        request.setPageSize(1);
        request.setPatientId(patientId);

        stats.put("visitCount", followUpService.getList(request).getTotal());
        stats.put("adviceCount", medicationService.getList(request).getTotal());
        stats.put("pendingFollowUps", followUpService.getList(request).getTotal());

        return stats;
    }

    @Override
    public Map<String, Object> getStatsByRole(Long userId, String role) {
        if (userId == null) {
            return new HashMap<>();
        }

        // 管理员看全院数据
        if (permissionService.isAdmin(userId)) {
            return getAdminStats();
        }

        // 医生只看自己的数据
        if ("doctor".equalsIgnoreCase(role)) {
            return getDoctorStats(userId);
        }

        // 患者只看自己的数据
        if ("patient".equalsIgnoreCase(role)) {
            return getPatientStats(userId);
        }

        return new HashMap<>();
    }
}