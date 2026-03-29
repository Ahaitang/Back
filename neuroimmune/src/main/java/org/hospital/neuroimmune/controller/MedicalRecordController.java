package org.hospital.neuroimmune.controller;

import org.hospital.neuroimmune.common.Result;
import org.hospital.neuroimmune.dto.PageRequest;
import org.hospital.neuroimmune.dto.PageResult;
import org.hospital.neuroimmune.entity.MedicalRecord;
import org.hospital.neuroimmune.service.MedicalRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/records")
@CrossOrigin
public class MedicalRecordController {

    @Autowired
    private MedicalRecordService medicalRecordService;

    /**
     * 获取病历列表
     * 管理员看全部，医生只看自己患者的病历，患者只看自己的
     */
    @GetMapping
    public Result<PageResult<MedicalRecord>> list(
            PageRequest request,
            @RequestParam(required = false) Long patientId,
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {

        // 如果指定了patientId，按patientId查询
        if (patientId != null) {
            request.setPatientId(patientId);
            return Result.success(medicalRecordService.getList(request));
        }

        // 医生角色只看自己患者的病历
        if ("doctor".equals(role) && userId != null) {
            return Result.success(medicalRecordService.getListByDoctorId(userId, request));
        }

        // 患者角色只看自己的病历
        if ("patient".equals(role) && userId != null) {
            request.setPatientId(userId);
        }

        return Result.success(medicalRecordService.getList(request));
    }

    /**
     * 获取患者的病历列表（患者端用）
     */
    @GetMapping("/patient/{patientId}")
    public Result<PageResult<MedicalRecord>> listByPatient(
            @PathVariable Long patientId,
            PageRequest request,
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        // 患者只能查看自己的病历
        if ("patient".equals(role) && userId != null && !userId.equals(patientId)) {
            return Result.error("无权查看其他患者信息");
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

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        medicalRecordService.delete(id);
        return Result.success();
    }
}