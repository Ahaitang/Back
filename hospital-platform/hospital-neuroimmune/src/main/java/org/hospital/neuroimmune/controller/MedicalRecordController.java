package org.hospital.neuroimmune.controller;

import org.hospital.common.model.Result;
import org.hospital.common.model.PageRequest;
import org.hospital.common.model.PageResult;
import org.hospital.common.security.SecurityContextHelper;
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
            @RequestParam(required = false) String status) {

        Long currentUserId = SecurityContextHelper.getCurrentUserId();

        if (patientId != null) {
            String error = checkPatientAccess(patientId);
            if (error != null) return Result.error(403, error);
            request.setPatientId(patientId);
            return Result.success(medicalRecordService.getList(request));
        }

        if (SecurityContextHelper.isDoctor() && currentUserId != null) {
            return Result.success(medicalRecordService.getListByDoctorId(currentUserId, request));
        }

        if (SecurityContextHelper.isPatient() && currentUserId != null) {
            request.setPatientId(currentUserId);
        }
        if (status != null && !status.isEmpty()) {
            request.setStatus(status);
        }

        return Result.success(medicalRecordService.getList(request));
    }

    @GetMapping("/{id}")
    public Result<MedicalRecord> getById(@PathVariable Long id) {
        MedicalRecord record = medicalRecordService.getById(id);
        String error = checkPatientAccess(record != null ? record.getPatientId() : null);
        if (error != null) return Result.error(403, error);
        return Result.success(record);
    }

    @PostMapping
    public Result<Void> save(@RequestBody MedicalRecord record) {
        String error = checkPatientAccess(record.getPatientId());
        if (error != null) return Result.error(403, error);
        medicalRecordService.save(record);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody MedicalRecord record) {
        MedicalRecord existing = medicalRecordService.getById(id);
        String error = checkPatientAccess(existing != null ? existing.getPatientId() : record.getPatientId());
        if (error != null) return Result.error(403, error);
        record.setId(id);
        medicalRecordService.save(record);
        return Result.success();
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        MedicalRecord existing = medicalRecordService.getById(id);
        String error = checkPatientAccess(existing != null ? existing.getPatientId() : null);
        if (error != null) return Result.error(403, error);
        medicalRecordService.updateStatus(id, status);
        return Result.success();
    }

    @PutMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id) {
        MedicalRecord existing = medicalRecordService.getById(id);
        String error = checkPatientAccess(existing != null ? existing.getPatientId() : null);
        if (error != null) return Result.error(403, error);
        medicalRecordService.cancel(id);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        MedicalRecord existing = medicalRecordService.getById(id);
        String error = checkPatientAccess(existing != null ? existing.getPatientId() : null);
        if (error != null) return Result.error(403, error);
        medicalRecordService.delete(id);
        return Result.success();
    }

    private String checkPatientAccess(Long patientId) {
        if (patientId == null) {
            return "记录不存在";
        }
        return permissionService.checkPatientAccessPermission(
                SecurityContextHelper.getCurrentUserId(),
                SecurityContextHelper.getCurrentRole(),
                patientId);
    }
}
