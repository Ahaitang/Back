package org.hospital.neuroimmune.controller;

import org.hospital.common.model.Result;
import org.hospital.common.model.PageRequest;
import org.hospital.common.model.PageResult;
import org.hospital.neuroimmune.entity.MedicalRecord;
import org.hospital.neuroimmune.service.MedicalRecordService;
import org.hospital.neuroimmune.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 病历控制器
 * 已重构：使用 PermissionService 处理权限检查
 */
@RestController
@RequestMapping("/api/v1/neuroimmune/records")
@CrossOrigin
public class MedicalRecordController {

    @Autowired
    private MedicalRecordService medicalRecordService;

    @Autowired
    private PermissionService permissionService;

    @GetMapping
    public Result<PageResult<MedicalRecord>> list(
            PageRequest request,
            @RequestParam(required = false) Long patientId,
            @RequestParam(required = false) String status,
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {

        if (patientId != null) {
            request.setPatientId(patientId);
            return Result.success(medicalRecordService.getList(request));
        }

        if ("doctor".equals(role) && userId != null) {
            return Result.success(medicalRecordService.getListByDoctorId(userId, request));
        }

        if ("patient".equals(role) && userId != null) {
            request.setPatientId(userId);
        }
        if (status != null && !status.isEmpty()) {
            request.setStatus(status);
        }

        return Result.success(medicalRecordService.getList(request));
    }

    @GetMapping("/patient/{patientId}")
    public Result<PageResult<MedicalRecord>> listByPatient(
            @PathVariable Long patientId,
            PageRequest request,
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        // 使用 PermissionService 检查权限
        String error = permissionService.checkPatientAccessPermission(userId, role, patientId);
        if (error != null) {
            return Result.error(error);
        }
        request.setPatientId(patientId);
        return Result.success(medicalRecordService.getList(request));
    }

    @GetMapping("/{id}")
    public Result<MedicalRecord> getById(@PathVariable Long id) {
        return Result.success(medicalRecordService.getById(id));
    }

    @PostMapping
    public Result<Void> save(@RequestBody MedicalRecord record) {
        medicalRecordService.save(record);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody MedicalRecord record) {
        record.setId(id);
        medicalRecordService.save(record);
        return Result.success();
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        medicalRecordService.updateStatus(id, status);
        return Result.success();
    }

    @PutMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id) {
        medicalRecordService.cancel(id);
        return Result.success();
    }
}