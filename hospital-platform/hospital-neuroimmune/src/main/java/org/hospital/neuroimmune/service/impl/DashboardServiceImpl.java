package org.hospital.neuroimmune.service.impl;

import org.hospital.common.model.PageRequest;
import org.hospital.neuroimmune.service.DashboardService;
import org.hospital.neuroimmune.service.PatientService;
import org.hospital.neuroimmune.service.DoctorService;
import org.hospital.neuroimmune.service.FollowUpService;
import org.hospital.neuroimmune.service.MedicationService;
import org.hospital.neuroimmune.service.MedicalRecordService;
import org.hospital.neuroimmune.service.PermissionService;
import org.hospital.neuroimmune.service.DiseaseEpisodeService;
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
    private MedicalRecordService medicalRecordService;

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private DiseaseEpisodeService diseaseEpisodeService;

    @Override
    public Map<String, Object> getAdminStats() {
        Map<String, Object> stats = new HashMap<>();
        PageRequest request = new PageRequest();
        request.setPageSize(1);

        stats.put("totalPatients", patientService.getList(request).getTotal());
        stats.put("totalDoctors", doctorService.getDoctorList(request).getTotal());
        stats.put("pendingFollowUps", followUpService.getPendingCount());
        stats.put("completedFollowUps", followUpService.getCompletedCount());
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
        stats.put("pendingFollowUps", followUpService.getPendingCountByDoctorId(doctorId));
        stats.put("completedFollowUps", followUpService.getCompletedCountByDoctorId(doctorId));
        stats.put("totalMedications", medicationService.getList(doctorRequest).getTotal());
        stats.put("totalMedicalRecords", medicalRecordService.countByDoctorId(doctorId));
        stats.put("totalEpisodes", diseaseEpisodeService.countByDoctorId(doctorId));

        return stats;
    }

    @Override
    public Map<String, Object> getPatientStats(Long patientId) {
        Map<String, Object> stats = new HashMap<>();
        PageRequest request = new PageRequest();
        request.setPageSize(1);
        request.setPatientId(patientId);

        stats.put("visitCount", medicalRecordService.countByPatientId(patientId));
        stats.put("adviceCount", medicationService.getList(request).getTotal());
        stats.put("pendingFollowUps", followUpService.getList(request).getTotal());

        return stats;
    }

    @Override
    public Map<String, Object> getStatsByRole(Long userId, String role) {
        if (userId == null) {
            return new HashMap<>();
        }

        // 根据 JWT 中的角色直接路由，避免跨表 ID 冲突
        // （患者 ID 和医生 ID 来自不同表，可能重合）
        return switch (role != null ? role.toLowerCase() : "") {
            case "patient" -> getPatientStats(userId);
            case "doctor" -> {
                // 医生可能同时拥有管理员角色
                if (permissionService.isAdmin(userId)) {
                    yield getAdminStats();
                }
                yield getDoctorStats(userId);
            }
            case "admin" -> getAdminStats();
            default -> new HashMap<>();
        };
    }
}