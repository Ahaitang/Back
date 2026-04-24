package org.hospital.neuroimmune.controller;

import org.hospital.common.model.Result;
import org.hospital.common.model.PageRequest;
import org.hospital.common.model.PageResult;
import org.hospital.neuroimmune.entity.FollowUp;
import org.hospital.neuroimmune.service.FollowUpService;
import org.hospital.neuroimmune.service.PatientDoctorRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/neuroimmune/followups")
@CrossOrigin
public class FollowUpController {

    @Autowired
    private FollowUpService followUpService;

    @Autowired
    private PatientDoctorRelationService relationService;

    @GetMapping
    public Result<PageResult<FollowUp>> list(
            PageRequest request,
            @RequestParam(required = false) Long patientId,
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) String status,
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {

        if ("doctor".equals(role) && userId != null) {
            request.setDoctorId(userId);
        } else if ("patient".equals(role) && userId != null) {
            request.setPatientId(userId);
        } else {
            if (patientId != null) request.setPatientId(patientId);
            if (doctorId != null) request.setDoctorId(doctorId);
        }
        if (status != null && !status.isEmpty()) {
            request.setStatus(status);
        }

        return Result.success(followUpService.getList(request));
    }

    @GetMapping("/patient/{patientId}")
    public Result<PageResult<FollowUp>> listByPatient(
            @PathVariable Long patientId,
            PageRequest request,
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if ("patient".equals(role) && userId != null && !userId.equals(patientId)) {
            return Result.error("无权查看其他患者信息");
        }
        request.setPatientId(patientId);
        return Result.success(followUpService.getList(request));
    }

    @GetMapping("/doctor/{doctorId}")
    public Result<PageResult<FollowUp>> listByDoctor(
            @PathVariable Long doctorId,
            PageRequest request) {
        request.setDoctorId(doctorId);
        return Result.success(followUpService.getList(request));
    }

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
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        followUpService.updateStatus(id, status);
        return Result.success();
    }

    @PutMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id) {
        followUpService.cancel(id);
        return Result.success();
    }
}