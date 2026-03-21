package org.hospital.neuroimmune.controller;

import org.hospital.neuroimmune.common.Result;
import org.hospital.neuroimmune.dto.PageRequest;
import org.hospital.neuroimmune.dto.PageResult;
import org.hospital.neuroimmune.entity.Medication;
import org.hospital.neuroimmune.service.MedicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/medications")
@CrossOrigin
public class MedicationController {

    @Autowired
    private MedicationService medicationService;

    /**
     * 获取用药列表
     * 管理员看全部，医生只看自己开出的用药记录，患者只看自己的
     */
    @GetMapping
    public Result<PageResult<Medication>> list(
            PageRequest request,
            @RequestParam(required = false) Long patientId,
            @RequestParam(required = false) Long doctorId,
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {

        // 医生角色只看自己开出的用药记录
        if ("doctor".equals(role) && userId != null) {
            request.setDoctorId(userId);
        } else if ("patient".equals(role) && userId != null) {
            // 患者角色只看自己的用药记录
            request.setPatientId(userId);
        } else {
            if (patientId != null) {
                request.setPatientId(patientId);
            }
            if (doctorId != null) {
                request.setDoctorId(doctorId);
            }
        }

        return Result.success(medicationService.getList(request));
    }

    /**
     * 获取患者的用药记录（患者端用）
     */
    @GetMapping("/patient/{patientId}")
    public Result<PageResult<Medication>> listByPatient(
            @PathVariable Long patientId,
            PageRequest request,
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        // 患者只能查看自己的用药记录
        if ("patient".equals(role) && userId != null && !userId.equals(patientId)) {
            return Result.error("无权查看其他患者信息");
        }
        request.setPatientId(patientId);
        return Result.success(medicationService.getList(request));
    }

    /**
     * 获取医生开出的用药记录（医生端用）
     */
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

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        medicationService.delete(id);
        return Result.success();
    }
}