package org.hospital.neuroimmune.controller;

import org.hospital.common.model.Result;
import org.hospital.common.model.PageRequest;
import org.hospital.neuroimmune.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/neuroimmune/dashboard")
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

    @Autowired
    private DoctorRoleService doctorRoleService;

    @GetMapping("/stats")
    public Result<Map<String, Object>> stats(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        Map<String, Object> stats = new HashMap<>();

        PageRequest request = new PageRequest();
        request.setPageSize(1);

        // 判断用户是否有管理员权限
        boolean isAdmin = false;
        if (userId != null) {
            List<String> roles = doctorRoleService.getRoleCodesByDoctorId(userId);
            isAdmin = roles != null && roles.contains("ADMIN");
        }

        // 管理员看全院数据 - 全部用分页查询 getTotal()
        // 医生数目只统计有 DOCTOR 权限的用户
        if (isAdmin) {
            stats.put("totalPatients", patientService.getList(request).getTotal());
            stats.put("totalDoctors", doctorService.getDoctorList(request).getTotal());
            stats.put("pendingFollowUps", followUpService.getList(request).getTotal());
            stats.put("totalMedications", medicationService.getList(request).getTotal());

        } else if ("doctor".equals(role) && userId != null) {
            // 医生只看自己的数据
            stats.put("totalPatients", patientService.getListByDoctorId(userId, request).getTotal());

            PageRequest doctorRequest = new PageRequest();
            doctorRequest.setPageSize(1);
            doctorRequest.setDoctorId(userId);
            stats.put("pendingFollowUps", followUpService.getList(doctorRequest).getTotal());
            stats.put("totalMedications", medicationService.getList(doctorRequest).getTotal());

        } else if ("patient".equals(role) && userId != null) {
            // 患者只看自己的数据
            request.setPatientId(userId);
            stats.put("visitCount", followUpService.getList(request).getTotal());
            stats.put("adviceCount", medicationService.getList(request).getTotal());
            stats.put("pendingFollowUps", followUpService.getList(request).getTotal());
        }

        return Result.success(stats);
    }
}