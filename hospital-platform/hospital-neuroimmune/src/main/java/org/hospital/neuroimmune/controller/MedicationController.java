package org.hospital.neuroimmune.controller;

import org.hospital.common.model.Result;
import org.hospital.common.model.PageRequest;
import org.hospital.common.model.PageResult;
import org.hospital.common.security.SecurityContextHelper;
import org.hospital.neuroimmune.entity.Medication;
import org.hospital.neuroimmune.service.MedicationService;
import org.hospital.neuroimmune.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用药控制器
 * 已重构：使用 PermissionService 处理权限检查
 */
@RestController
@RequestMapping("/api/v1/neuroimmune/medications")
@CrossOrigin
public class MedicationController {

    @Autowired
    private MedicationService medicationService;

    @Autowired
    private PermissionService permissionService;

    @GetMapping
    public Result<PageResult<Medication>> list(
            PageRequest request,
            @RequestParam(required = false) Long patientId,
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) String status) {

        Long currentUserId = SecurityContextHelper.getCurrentUserId();

        if (SecurityContextHelper.isDoctor() && currentUserId != null) {
            request.setDoctorId(currentUserId);
        } else if (SecurityContextHelper.isPatient() && currentUserId != null) {
            request.setPatientId(currentUserId);
        } else {
            if (patientId != null) request.setPatientId(patientId);
            if (doctorId != null) request.setDoctorId(doctorId);
        }
        if (status != null && !status.isEmpty()) {
            request.setStatus(status);
        }

        return Result.success(medicationService.getList(request));
    }

    @GetMapping("/patient/{patientId}")
    public Result<PageResult<Medication>> listByPatient(
            @PathVariable Long patientId,
            PageRequest request) {
        // 使用 PermissionService 检查权限
        String error = permissionService.checkPatientAccessPermission(
                SecurityContextHelper.getCurrentUserId(),
                SecurityContextHelper.getCurrentRole(),
                patientId);
        if (error != null) {
            return Result.error(error);
        }
        request.setPatientId(patientId);
        return Result.success(medicationService.getList(request));
    }

    @GetMapping("/doctor/{doctorId}")
    public Result<PageResult<Medication>> listByDoctor(
            @PathVariable Long doctorId,
            PageRequest request) {
        request.setDoctorId(doctorId);
        return Result.success(medicationService.getList(request));
    }

    @GetMapping("/{id}")
    public Result<Medication> getById(@PathVariable Long id) {
        return Result.success(medicationService.getById(id));
    }

    @PostMapping
    public Result<Void> save(@RequestBody Medication medication) {
        medicationService.save(medication);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Medication medication) {
        medication.setId(id);
        medicationService.save(medication);
        return Result.success();
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        medicationService.updateStatus(id, status);
        return Result.success();
    }

    @PutMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id) {
        medicationService.cancel(id);
        return Result.success();
    }
}