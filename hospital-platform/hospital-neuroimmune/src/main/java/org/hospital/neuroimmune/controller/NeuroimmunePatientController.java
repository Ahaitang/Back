package org.hospital.neuroimmune.controller;

import org.hospital.common.model.Result;
import org.hospital.common.model.PageRequest;
import org.hospital.common.model.PageResult;
import org.hospital.neuroimmune.entity.Patient;
import org.hospital.neuroimmune.service.PatientService;
import org.hospital.neuroimmune.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 患者控制器
 * 已重构：使用 PermissionService 处理权限检查，简化 Controller 逻辑
 * API 合并：
 * - GET /patients/my → 使用 GET /patients?mine=true
 */
@RestController
@RequestMapping("/api/v1/neuroimmune/patients")
@CrossOrigin
public class NeuroimmunePatientController {

    @Autowired
    private PatientService patientService;

    @Autowired
    private PermissionService permissionService;

    /**
     * 获取患者列表（统一接口）
     * 支持参数：
     * - mine: boolean，设置为 true 返回当前医生的患者列表
     * - doctorId: 按医生筛选
     * - keyword: 关键词搜索
     * - gender: 性别筛选
     * - isRealAuth: 实名认证筛选
     * - type: 疾病类型筛选
     */
    @GetMapping
    public Result<PageResult<Patient>> list(
            PageRequest request,
            @RequestParam(required = false) Boolean mine,
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {

        // 如果请求自己的患者列表
        if (mine != null && mine && "doctor".equals(role) && userId != null) {
            return Result.success(patientService.getListByDoctorId(userId, request));
        }

        // 如果请求中指定了 doctorId，返回该医生的患者
        if (request.getDoctorId() != null && request.getDoctorId() > 0) {
            return Result.success(patientService.getListByDoctorId(request.getDoctorId(), request));
        }
        // 如果是医生角色且userId有效，只能看到自己的患者
        if ("doctor".equals(role) && userId != null && userId > 0) {
            return Result.success(patientService.getListByDoctorId(userId, request));
        }
        // 如果是患者角色且userId有效，只能看到自己的信息
        if ("patient".equals(role) && userId != null && userId > 0) {
            return Result.success(patientService.getByIdAsPageResult(userId));
        }
        // 管理员或其他情况，返回所有患者
        return Result.success(patientService.getList(request));
    }

    @GetMapping("/{id}")
    public Result<Patient> getById(@PathVariable Long id,
                                   @RequestHeader(value = "X-User-Role", required = false) String role,
                                   @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        // 使用 PermissionService 检查权限
        String error = permissionService.checkPatientAccessPermission(userId, role, id);
        if (error != null) {
            return Result.error(error);
        }
        return Result.success(patientService.getById(id));
    }

    @PostMapping
    public Result<Long> save(@RequestBody Patient patient) {
        try {
            patientService.save(patient);
            return Result.success(patient.getId());
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Patient patient) {
        patient.setId(id);
        patientService.save(patient);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        patientService.delete(id);
        return Result.success();
    }
}