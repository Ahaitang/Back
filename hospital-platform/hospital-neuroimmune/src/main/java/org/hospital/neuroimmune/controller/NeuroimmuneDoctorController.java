package org.hospital.neuroimmune.controller;

import org.hospital.common.model.Result;
import org.hospital.common.model.PageRequest;
import org.hospital.common.model.PageResult;
import org.hospital.neuroimmune.entity.Doctor;
import org.hospital.neuroimmune.service.DoctorService;
import org.hospital.neuroimmune.service.DoctorRoleService;
import org.hospital.neuroimmune.service.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/neuroimmune/doctors")
@CrossOrigin
public class NeuroimmuneDoctorController {

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private DoctorRoleService doctorRoleService;

    @Autowired
    private PatientService patientService;

    /**
     * 医生列表（只返回 role 包含 doctor 的用户）
     */
    @GetMapping
    public Result<PageResult<Doctor>> list(PageRequest request) {
        return Result.success(doctorService.getDoctorList(request));
    }

    /**
     * 管理员列表（只返回 role 包含 admin 的用户）
     */
    @GetMapping("/admins")
    public Result<PageResult<Doctor>> adminList(PageRequest request) {
        return Result.success(doctorService.getAdminList(request));
    }

    /**
     * 所有医生（用于下拉选择）
     */
    @GetMapping("/all")
    public Result<List<Doctor>> all() {
        PageRequest request = new PageRequest();
        request.setPageSize(10000);
        return Result.success(doctorService.getDoctorList(request).getList());
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
     * @param id 用户ID
     * @param request 角色和等级请求体
     */
    @PutMapping("/{id}/roles")
    public Result<Void> updateRoles(@PathVariable Long id,
                                    @RequestBody RoleUpdateRequest request,
                                    @RequestHeader(value = "X-User-Id", required = false) Long currentUserId) {
        List<String> roleCodes = request.getRoles();
        Integer level = request.getLevel();

        // 权限检查
        Doctor currentUser = doctorService.getById(currentUserId);
        if (currentUser == null) {
            return Result.error("用户信息获取失败");
        }

        Doctor targetUser = doctorService.getById(id);
        if (targetUser == null) {
            return Result.error("用户不存在");
        }

        // 获取当前用户角色
        List<String> currentUserRoles = doctorRoleService.getRoleCodesByDoctorId(currentUserId);
        boolean currentUserIsAdmin = currentUserRoles.contains("ADMIN");

        // 修改自己的角色
        if (id.equals(currentUserId)) {
            // 非管理员不能把自己设置为管理员
            if (!currentUserIsAdmin && roleCodes.contains("ADMIN")) {
                return Result.error("非管理员不能将自己设置为管理员");
            }
            // 管理员不能移除自己的管理员角色（防止意外锁死）
            if (currentUserIsAdmin && !roleCodes.contains("ADMIN")) {
                return Result.error("管理员不能移除自己的管理员角色");
            }
            // 可以修改自己的其他角色（同级修改）
            doctorRoleService.setDoctorRoles(id, roleCodes);
            // 同时更新等级
            if (level != null) {
                doctorService.updateRoleAndLevel(id, null, level);
            }
            return Result.success();
        }

        // 修改别人的角色 - 必须是管理员
        if (!currentUserIsAdmin) {
            return Result.error("只有管理员可以修改其他用户的角色");
        }

        // 管理员不能修改同级或更高等级的用户
        Integer currentLevel = currentUser.getLevel();
        Integer targetLevel = targetUser.getLevel();

        // level 数字越小权限越高，null 表示普通医生
        if (currentLevel == null) currentLevel = 999; // 普通管理员默认最低
        if (targetLevel == null) targetLevel = 999;

        if (currentLevel >= targetLevel) {
            return Result.error("无权限修改同级或更高等级用户");
        }

        doctorRoleService.setDoctorRoles(id, roleCodes);
        // 同时更新等级
        if (level != null) {
            doctorService.updateRoleAndLevel(id, null, level);
        }
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
        Long patientCount = patientService.countByDoctorId(id);
        if (patientCount > 0) {
            return Result.error("该医生下有 " + patientCount + " 名患者，无法删除");
        }
        doctorService.delete(id);
        return Result.success();
    }
}