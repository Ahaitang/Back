package org.hospital.neuroimmune.controller;

import org.hospital.neuroimmune.common.Result;
import org.hospital.neuroimmune.dto.PageRequest;
import org.hospital.neuroimmune.dto.PageResult;
import org.hospital.neuroimmune.entity.FollowUp;
import org.hospital.neuroimmune.service.FollowUpService;
import org.hospital.neuroimmune.service.PatientDoctorRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/neuroimmune/followups")
@CrossOrigin
public class FollowUpController {

    @Autowired
    private FollowUpService followUpService;

    @Autowired
    private PatientDoctorRelationService relationService;

    /**
     * 获取随访列表
     * 管理员看全部，医生只看自己的，患者只看自己的
     */
    @GetMapping
    public Result<PageResult<FollowUp>> list(
            PageRequest request,
            @RequestParam(required = false) Long patientId,
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) String status,
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {

        // 医生角色只看自己的随访
        if ("doctor".equals(role) && userId != null) {
            request.setDoctorId(userId);
        } else if ("patient".equals(role) && userId != null) {
            // 患者角色只看自己的随访
            request.setPatientId(userId);
        } else {
            if (patientId != null) {
                request.setPatientId(patientId);
            }
            if (doctorId != null) {
                request.setDoctorId(doctorId);
            }
        }
        if (status != null) {
            request.setStatus(status);
        }

        return Result.success(followUpService.getList(request));
    }

    /**
     * 获取患者的随访列表（患者端用）
     */
    @GetMapping("/patient/{patientId}")
    public Result<PageResult<FollowUp>> listByPatient(
            @PathVariable Long patientId,
            PageRequest request,
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        // 患者只能查看自己的随访
        if ("patient".equals(role) && userId != null && !userId.equals(patientId)) {
            return Result.error("无权查看其他患者信息");
        }
        request.setPatientId(patientId);
        return Result.success(followUpService.getList(request));
    }

    /**
     * 获取医生的随访列表（医生端用）
     */
    @GetMapping("/doctor/{doctorId}")
    public Result<PageResult<FollowUp>> listByDoctor(
            @PathVariable Long doctorId,
            PageRequest request) {
        request.setDoctorId(doctorId);
        return Result.success(followUpService.getList(request));
    }

    /**
     * 获取医生待随访列表
     */
    @GetMapping("/doctor/{doctorId}/pending")
    public Result<List<FollowUp>> getPendingByDoctor(@PathVariable Long doctorId) {
        return Result.success(followUpService.getPendingByDoctorId(doctorId));
    }

    @GetMapping("/{id}")
    public Result<FollowUp> getById(@PathVariable Long id) {
        return Result.success(followUpService.getById(id));
    }

    @PostMapping
    public Result<Void> save(@RequestBody FollowUp followUp) {
        followUpService.save(followUp);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody FollowUp followUp) {
        followUp.setId(id);
        followUpService.save(followUp);
        return Result.success();
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam String status) {
        followUpService.updateStatus(id, status);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        followUpService.delete(id);
        return Result.success();
    }
}