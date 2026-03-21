package org.hospital.neuroimmune.controller;

import org.hospital.neuroimmune.common.Result;
import org.hospital.neuroimmune.dto.PageRequest;
import org.hospital.neuroimmune.dto.PageResult;
import org.hospital.neuroimmune.entity.Patient;
import org.hospital.neuroimmune.service.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/patients")
@CrossOrigin
public class PatientController {

    @Autowired
    private PatientService patientService;

    /**
     * 获取患者列表（管理员用）
     * 支持通过 doctorId 参数过滤
     */
    @GetMapping
    public Result<PageResult<Patient>> list(PageRequest request,
                                            @RequestHeader(value = "X-User-Role", required = false) String role,
                                            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
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

    /**
     * 获取医生自己的患者列表
     */
    @GetMapping("/my")
    public Result<PageResult<Patient>> getMyPatients(
            @RequestHeader("X-User-Id") Long doctorId,
            PageRequest request) {
        return Result.success(patientService.getListByDoctorId(doctorId, request));
    }

    @GetMapping("/{id}")
    public Result<Patient> getById(@PathVariable Long id,
                                   @RequestHeader(value = "X-User-Role", required = false) String role,
                                   @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        // 患者只能查看自己的信息
        if ("patient".equals(role) && userId != null && !userId.equals(id)) {
            return Result.error("无权查看其他患者信息");
        }
        return Result.success(patientService.getById(id));
    }

    @PostMapping
    public Result<Void> save(@RequestBody Patient patient) {
        patientService.save(patient);
        return Result.success();
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