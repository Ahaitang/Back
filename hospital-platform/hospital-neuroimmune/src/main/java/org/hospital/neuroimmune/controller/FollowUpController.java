package org.hospital.neuroimmune.controller;

import org.hospital.common.model.Result;
import org.hospital.common.model.PageRequest;
import org.hospital.common.model.PageResult;
import org.hospital.common.security.SecurityContextHelper;
import org.hospital.neuroimmune.entity.FollowUp;
import org.hospital.neuroimmune.service.FollowUpService;
import org.hospital.neuroimmune.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 随访控制器
 * 已重构：使用 PermissionService 处理权限检查
 * API 合并：删除冗余路径，统一使用查询参数
 */
@RestController
@RequestMapping("/api/v1/neuroimmune/followups")
@CrossOrigin
public class FollowUpController {

    @Autowired
    private FollowUpService followUpService;

    @Autowired
    private PermissionService permissionService;

    /**
     * 获取随访列表（统一接口）
     * 支持参数：
     * - patientId: 按患者筛选
     * - doctorId: 按医生筛选
     * - status: 按状态筛选
     *
     * 原路径（已删除）：
     * - GET /followups/patient/{id} → 使用 GET /followups?patientId={id}
     * - GET /followups/doctor/{id} → 使用 GET /followups?doctorId={id}
     */
    @GetMapping
    public Result<PageResult<FollowUp>> list(
            PageRequest request,
            @RequestParam(required = false) Long patientId,
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) String status) {

        Long currentUserId = SecurityContextHelper.getCurrentUserId();
        String currentRole = SecurityContextHelper.getCurrentRole();

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

        return Result.success(followUpService.getList(request));
    }

    /**
     * 获取医生的待处理随访
     */
    @GetMapping("/pending")
    public Result<List<FollowUp>> getPending(
            @RequestParam(required = false) Long doctorId) {
        // 如果指定了 doctorId，使用它；否则使用当前用户
        Long targetDoctorId = doctorId != null ? doctorId : SecurityContextHelper.getCurrentUserId();
        if (targetDoctorId == null) {
            return Result.error("需要指定医生ID");
        }
        return Result.success(followUpService.getPendingByDoctorId(targetDoctorId));
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