package org.hospital.neuroimmune.controller;

import org.hospital.common.model.Result;
import org.hospital.common.model.PageRequest;
import org.hospital.common.model.PageResult;
import org.hospital.neuroimmune.entity.Doctor;
import org.hospital.neuroimmune.service.DoctorService;
import org.hospital.neuroimmune.service.DoctorRoleService;
import org.hospital.neuroimmune.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 医生控制器
 * 已重构：使用 PermissionService 处理权限逻辑，业务逻辑下沉到 Service 层
 * API 合并：
 * - GET /doctors/all → 使用 GET /doctors?pageSize=all 或 GET /doctors?all=true
 */
@RestController
@RequestMapping("/api/v1/neuroimmune/doctors")
@CrossOrigin
public class NeuroimmuneDoctorController {

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private DoctorRoleService doctorRoleService;

    @Autowired
    private PermissionService permissionService;

    /**
     * 医生列表（统一接口）
     * 支持参数：
     * - pageSize: 分页大小，默认10。传 "all" 或设置 all=true 返回全部
     * - all: boolean，设置为 true 返回全部医生（用于下拉选择）
     * - keyword: 关键词搜索
     * - department: 科室筛选
     */
    @GetMapping
    public Result<?> list(
            PageRequest request,
            @RequestParam(required = false) Boolean all) {
        // 如果请求全部数据（用于下拉选择）
        if (all != null && all) {
            PageRequest fullRequest = new PageRequest();
            fullRequest.setPageSize(10000);
            return Result.success(doctorService.getDoctorList(fullRequest).getList());
        }
        return Result.success(doctorService.getDoctorList(request));
    }

    /**
     * 管理员列表（只返回 role 包含 admin 的用户）
     */
    @GetMapping("/admins")
    public Result<PageResult<Doctor>> adminList(PageRequest request) {
        return Result.success(doctorService.getAdminList(request));
    }

    @GetMapping("/{id}")
    public Result<Doctor> getById(@PathVariable Long id) {
        return Result.success(doctorService.getById(id));
    }

    @PostMapping
    public Result<Void> save(@RequestBody Doctor doctor) {
        doctorService.save(doctor);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Doctor doctor) {
        doctor.setId(id);
        doctorService.save(doctor);
        return Result.success();
    }

    /**
     * 获取医生的角色列表
     */
    @GetMapping("/{id}/roles")
    public Result<List<String>> getRoles(@PathVariable Long id) {
        return Result.success(doctorRoleService.getRoleCodesByDoctorId(id));
    }

    /**
     * 更新医生的角色列表和管理等级
     * 权限检查逻辑已下沉到 PermissionService
     */
    @PutMapping("/{id}/roles")
    public Result<Void> updateRoles(@PathVariable Long id,
                                    @RequestBody RoleUpdateRequest request,
                                    @RequestHeader(value = "X-User-Id", required = false) Long currentUserId) {
        List<String> roleCodes = request.getRoles();
        Integer level = request.getLevel();

        // 使用 PermissionService 检查权限
        String error = permissionService.checkRoleUpdatePermission(currentUserId, id, roleCodes);
        if (error != null) {
            return Result.error(error);
        }

        // 更新角色和等级
        permissionService.updateUserRolesAndLevel(id, roleCodes, level);
        return Result.success();
    }

    /**
     * 角色和等级更新请求
     */
    public static class RoleUpdateRequest {
        private List<String> roles;
        private Integer level;

        public List<String> getRoles() { return roles; }
        public void setRoles(List<String> roles) { this.roles = roles; }
        public Integer getLevel() { return level; }
        public void setLevel(Integer level) { this.level = level; }
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        // 逻辑删除，会自动解绑患者关系
        doctorService.delete(id);
        return Result.success();
    }
}