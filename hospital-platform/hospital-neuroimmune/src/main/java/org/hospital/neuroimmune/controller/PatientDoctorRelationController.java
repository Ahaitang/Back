package org.hospital.neuroimmune.controller;

import org.hospital.neuroimmune.common.Result;
import org.hospital.neuroimmune.entity.PatientDoctorRelation;
import org.hospital.neuroimmune.entity.Patient;
import org.hospital.neuroimmune.service.PatientDoctorRelationService;
import org.hospital.neuroimmune.service.PatientService;
import org.hospital.neuroimmune.service.DoctorService;
import org.hospital.neuroimmune.entity.Doctor;
import org.hospital.neuroimmune.dto.PageRequest;
import org.hospital.neuroimmune.dto.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/neuroimmune/relation")
public class PatientDoctorRelationController {

    @Autowired
    private PatientDoctorRelationService relationService;

    @Autowired
    private PatientService patientService;

    @Autowired
    private DoctorService doctorService;

    /**
     * 患者绑定医生
     */
    @PostMapping("/bind")
    public Result<Map<String, Object>> bindDoctor(@RequestBody Map<String, Object> params) {
        Long patientId = Long.parseLong(params.get("patientId").toString());
        Long doctorId = Long.parseLong(params.get("doctorId").toString());
        String bindMethod = params.get("bindMethod") != null ? params.get("bindMethod").toString() : "patient";
        String remark = params.get("remark") != null ? params.get("remark").toString() : null;

        boolean success = relationService.bindDoctor(patientId, doctorId, bindMethod, remark);

        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("message", success ? "绑定成功" : "绑定失败");

        return Result.success(result);
    }

    /**
     * 解除绑定
     */
    @PostMapping("/unbind")
    public Result<Map<String, Object>> unbind(@RequestBody Map<String, Object> params) {
        Long id = Long.parseLong(params.get("id").toString());

        boolean success = relationService.unbind(id);

        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("message", success ? "解绑成功" : "解绑失败");

        return Result.success(result);
    }

    /**
     * 解除患者当前绑定
     */
    @PostMapping("/unbind-patient/{patientId}")
    public Result<Map<String, Object>> unbindPatient(@PathVariable Long patientId) {
        boolean success = relationService.unbindPatient(patientId);

        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("message", success ? "解绑成功" : "解绑失败或无绑定");

        return Result.success(result);
    }

    /**
     * 获取患者当前绑定的医生
     */
    @GetMapping("/patient/{patientId}/doctor")
    public Result<PatientDoctorRelation> getPatientDoctor(@PathVariable Long patientId) {
        PatientDoctorRelation relation = relationService.getActiveDoctor(patientId);
        return Result.success(relation);
    }

    /**
     * 获取医生的绑定患者列表
     */
    @GetMapping("/doctor/{doctorId}/patients")
    public Result<List<PatientDoctorRelation>> getDoctorPatients(@PathVariable Long doctorId) {
        List<PatientDoctorRelation> relations = relationService.getActivePatientsByDoctor(doctorId);
        return Result.success(relations);
    }

    /**
     * 获取医生的患者详情列表（含患者完整信息）
     */
    @GetMapping("/doctor/{doctorId}/patient-details")
    public Result<List<Map<String, Object>>> getDoctorPatientDetails(@PathVariable Long doctorId) {
        List<PatientDoctorRelation> relations = relationService.getActivePatientsByDoctor(doctorId);

        List<Map<String, Object>> result = relations.stream().map(relation -> {
            Map<String, Object> item = new HashMap<>();
            item.put("relationId", relation.getId());
            item.put("patientId", relation.getPatientId());
            item.put("patientName", relation.getPatientName());
            item.put("doctorName", relation.getDoctorName());
            item.put("bindTime", relation.getBindTime());

            // 获取患者详细信息
            Patient patient = patientService.getById(relation.getPatientId());
            if (patient != null) {
                item.put("gender", patient.getGender());
                item.put("age", patient.getAge());
                item.put("phone", patient.getPhone());
                item.put("hasFollowUp", patient.getHasFollowUp());
                item.put("isRealAuth", patient.getIsRealAuth());
                item.put("diseaseType", patient.getDiseaseType());
            }

            return item;
        }).collect(Collectors.toList());

        return Result.success(result);
    }

    /**
     * 获取患者的绑定历史
     */
    @GetMapping("/patient/{patientId}/history")
    public Result<List<PatientDoctorRelation>> getPatientHistory(@PathVariable Long patientId) {
        List<PatientDoctorRelation> relations = relationService.getBindHistory(patientId);
        return Result.success(relations);
    }

    /**
     * 获取所有绑定关系（管理端）
     */
    @GetMapping("/list")
    public Result<List<PatientDoctorRelation>> getList(
            @RequestParam(required = false) String patientName,
            @RequestParam(required = false) String doctorName,
            @RequestParam(required = false) String status) {
        List<PatientDoctorRelation> relations = relationService.getList(patientName, doctorName, status);
        return Result.success(relations);
    }

    /**
     * 统计医生的患者数量
     */
    @GetMapping("/doctor/{doctorId}/count")
    public Result<Long> countDoctorPatients(@PathVariable Long doctorId) {
        Long count = relationService.countPatientsByDoctor(doctorId);
        return Result.success(count);
    }

    /**
     * 获取所有医生列表（供患者选择）
     */
    @GetMapping("/doctors")
    public Result<List<Doctor>> getDoctorList() {
        PageResult<Doctor> pageResult = doctorService.getList(new PageRequest());
        return Result.success(pageResult.getList());
    }

    /**
     * 获取绑定详情
     */
    @GetMapping("/{id}")
    public Result<PatientDoctorRelation> getById(@PathVariable Long id) {
        PatientDoctorRelation relation = relationService.getById(id);
        return Result.success(relation);
    }
}