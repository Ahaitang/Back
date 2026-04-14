package org.hospital.neuroimmune.controller;

import org.hospital.neuroimmune.common.Result;
import org.hospital.neuroimmune.dto.PageRequest;
import org.hospital.neuroimmune.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/neuroimmune/dashboard")
@CrossOrigin
public class DashboardController {

    @Autowired
    private PatientService patientService;

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private FollowUpService followUpService;

    @Autowired
    private MedicationService medicationService;

    @GetMapping("/stats")
    @Cacheable(value = "neuro-stats", key = "'dashboard:' + #role + ':' + #userId")
    public Result<Map<String, Object>> stats(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        Map<String, Object> stats = new HashMap<>();

        PageRequest request = new PageRequest();
        request.setPageSize(1); // 只需要总数，不需要查询列表

        // 医生角色只看自己的数据
        if ("doctor".equals(role) && userId != null) {
            stats.put("totalPatients", patientService.countByDoctorId(userId));
            stats.put("totalDoctors", 1); // 医生只看到自己
            stats.put("pendingFollowUps", followUpService.getPendingCountByDoctorId(userId));
            stats.put("totalMedications", medicationService.countByDoctorId(userId));
        } else if ("patient".equals(role) && userId != null) {
            // 患者只看自己的数据
            request.setPatientId(userId);
            stats.put("visitCount", followUpService.getList(request).getTotal());
            stats.put("adviceCount", medicationService.getList(request).getTotal());
            stats.put("pendingFollowUps", followUpService.getPendingCountByPatientId(userId));
        } else {
            // 管理员看全部
            stats.put("totalPatients", patientService.getList(request).getTotal());
            stats.put("totalDoctors", doctorService.getList(request).getTotal());
            stats.put("pendingFollowUps", followUpService.getPendingCount());
            stats.put("totalMedications", medicationService.getList(request).getTotal());
        }

        return Result.success(stats);
    }
}