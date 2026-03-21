package org.hospital.qmg.controller;

import lombok.extern.slf4j.Slf4j;
import org.hospital.qmg.entity.Patient;
import org.hospital.qmg.pojo.BatchImportResult;
import org.hospital.qmg.pojo.Result;
import org.hospital.qmg.service.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 患者Controller
 * 全部使用POST请求
 */
@Slf4j
@RestController
@RequestMapping("/api/patient")
@CrossOrigin(origins = "*")
public class PatientController {

    @Autowired
    private PatientService patientService;

    /**
     * 查询患者列表。
     * Web 端：不传 scope 时，管理员/超级管理员看全部，普通医生只看自己关联的患者。
     * App 端：传 scope=mine 时，所有人（含管理员）只看自己关联的患者。
     */
    @PostMapping("/list")
    public Result list(@RequestBody(required = false) Map<String, Object> params) {
        if (params == null) {
            params = new java.util.HashMap<>();
        }
        Integer currentDoctorId = params.get("currentDoctorId") != null
            ? ((Number) params.get("currentDoctorId")).intValue() : null;
        Integer currentUserLevel = params.get("currentUserLevel") != null
            ? ((Number) params.get("currentUserLevel")).intValue() : null;
        String scope = params.get("scope") != null ? params.get("scope").toString().trim() : null;

        if ("mine".equalsIgnoreCase(scope)) {
            log.info("查询患者列表(scope=mine)，当前医生ID: {}", currentDoctorId);
            List<Patient> patients = patientService.findByDoctorIdOnly(currentDoctorId);
            return Result.success(patients);
        }
        log.info("查询所有患者列表，当前医生ID: {}, 权限等级: {}", currentDoctorId, currentUserLevel);
        List<Patient> patients = patientService.findAll(currentDoctorId, currentUserLevel);
        return Result.success(patients);
    }

    /**
     * 根据ID查询患者（根据权限过滤）
     */
    @PostMapping("/getById")
    public Result getById(@RequestBody Map<String, Object> params) {
        Integer id = params.get("id") != null 
            ? ((Number) params.get("id")).intValue() : null;
        Integer currentDoctorId = params.get("currentDoctorId") != null 
            ? ((Number) params.get("currentDoctorId")).intValue() : null;
        Integer currentUserLevel = params.get("currentUserLevel") != null 
            ? ((Number) params.get("currentUserLevel")).intValue() : null;
        
        log.info("根据ID查询患者: {}, 当前医生ID: {}, 权限等级: {}", id, currentDoctorId, currentUserLevel);
        Patient patient = patientService.findById(id, currentDoctorId, currentUserLevel);
        if (patient != null) {
            return Result.success(patient);
        }
        return Result.error("患者不存在或无权限访问");
    }

    /**
     * 根据住院号查询患者
     */
    @PostMapping("/getByAdmissionNumber")
    public Result getByAdmissionNumber(@RequestBody Map<String, String> params) {
        String admissionNumber = params.get("admissionNumber");
        log.info("根据住院号查询患者: {}", admissionNumber);
        Patient patient = patientService.findByAdmissionNumber(admissionNumber);
        if (patient != null) {
            return Result.success(patient);
        }
        return Result.error("患者不存在");
    }

    /**
     * 搜索患者。
     * Web 端：不传 scope 时按权限过滤；App 端传 scope=mine 时只搜自己关联的患者。
     */
    @PostMapping("/search")
    public Result search(@RequestBody Map<String, Object> params) {
        String keyword = (String) params.get("keyword");
        Integer currentDoctorId = params.get("currentDoctorId") != null
            ? ((Number) params.get("currentDoctorId")).intValue() : null;
        Integer currentUserLevel = params.get("currentUserLevel") != null
            ? ((Number) params.get("currentUserLevel")).intValue() : null;
        String scope = params.get("scope") != null ? params.get("scope").toString().trim() : null;

        if ("mine".equalsIgnoreCase(scope)) {
            log.info("搜索患者(scope=mine)，关键词: {}, 当前医生ID: {}", keyword, currentDoctorId);
            List<Patient> patients = patientService.searchByDoctorIdAndKeywordOnly(currentDoctorId, keyword);
            return Result.success(patients);
        }
        log.info("搜索患者，关键词: {}, 当前医生ID: {}, 权限等级: {}", keyword, currentDoctorId, currentUserLevel);
        List<Patient> patients = patientService.searchByNameOrAdmissionNumber(keyword, currentDoctorId, currentUserLevel);
        return Result.success(patients);
    }

    /**
     * 新增患者。可选传 currentDoctorId，传入则为该医生建立患者关联（便于 Web 普通医生/App 端看到“自己添加的”患者）。
     */
    @PostMapping("/add")
    public Result add(@RequestBody Map<String, Object> params) {
        Patient patient = new Patient();
        patient.setName(params.get("name") != null ? params.get("name").toString().trim() : null);
        patient.setGender(params.get("gender") != null ? params.get("gender").toString().trim() : null);
        patient.setAdmissionNumber(params.get("admissionNumber") != null ? params.get("admissionNumber").toString().trim() : null);
        patient.setPhone(params.get("phone") != null ? params.get("phone").toString().trim() : null);
        Integer currentDoctorId = params.get("currentDoctorId") != null
            ? ((Number) params.get("currentDoctorId")).intValue() : null;

        log.info("新增患者: {}, currentDoctorId: {}", patient, currentDoctorId);

        if (patient.getName() == null || patient.getName().trim().isEmpty()) {
            return Result.error("患者姓名不能为空");
        }
        if (patient.getAdmissionNumber() == null || patient.getAdmissionNumber().trim().isEmpty()) {
            return Result.error("住院号不能为空");
        }
        if (patient.getPhone() == null || patient.getPhone().trim().isEmpty()) {
            return Result.error("手机号不能为空");
        }
        if (patient.getGender() == null || patient.getGender().trim().isEmpty()) {
            return Result.error("性别不能为空");
        }

        Patient existing = patientService.findByAdmissionNumber(patient.getAdmissionNumber());
        if (existing != null) {
            return Result.error("该住院号已存在");
        }

        patientService.save(patient);
        if (currentDoctorId != null) {
            patientService.linkDoctor(patient.getId(), currentDoctorId);
        }
        return Result.success("患者添加成功");
    }

    /**
     * 更新患者信息。传 currentUserLevel 时，管理员不可更新超级管理员添加的患者。
     */
    @PostMapping("/update")
    public Result update(@RequestBody Map<String, Object> params) {
        Integer id = params.get("id") != null ? ((Number) params.get("id")).intValue() : null;
        Integer currentUserLevel = params.get("currentUserLevel") != null ? ((Number) params.get("currentUserLevel")).intValue() : null;
        Patient patient = new Patient();
        patient.setId(id);
        patient.setName(params.get("name") != null ? params.get("name").toString().trim() : null);
        patient.setGender(params.get("gender") != null ? params.get("gender").toString().trim() : null);
        patient.setAdmissionNumber(params.get("admissionNumber") != null ? params.get("admissionNumber").toString().trim() : null);
        patient.setPhone(params.get("phone") != null ? params.get("phone").toString().trim() : null);

        log.info("更新患者信息: {}", patient.getId());
        if (patient.getId() == null) {
            return Result.error("患者ID不能为空");
        }
        if (currentUserLevel != null && currentUserLevel == 1) {
            Patient existing = patientService.findById(patient.getId(), null, 1);
            if (existing == null) {
                return Result.error("无权限修改该患者");
            }
        }
        if (patient.getName() == null || patient.getName().trim().isEmpty()) {
            return Result.error("患者姓名不能为空");
        }
        if (patient.getAdmissionNumber() == null || patient.getAdmissionNumber().trim().isEmpty()) {
            return Result.error("住院号不能为空");
        }
        if (patient.getPhone() == null || patient.getPhone().trim().isEmpty()) {
            return Result.error("手机号不能为空");
        }
        if (patient.getGender() == null || patient.getGender().trim().isEmpty()) {
            return Result.error("性别不能为空");
        }
        patientService.update(patient);
        return Result.success("患者信息更新成功");
    }

    /**
     * 删除患者。传 currentUserLevel 时，管理员不可删除超级管理员添加的患者。
     */
    @PostMapping("/delete")
    public Result delete(@RequestBody Map<String, Object> params) {
        Integer id = params.get("id") != null ? ((Number) params.get("id")).intValue() : null;
        Integer currentUserLevel = params.get("currentUserLevel") != null ? ((Number) params.get("currentUserLevel")).intValue() : null;
        log.info("删除患者: {}", id);
        if (id == null) {
            return Result.error("患者ID不能为空");
        }
        if (currentUserLevel != null && currentUserLevel == 1) {
            Patient existing = patientService.findById(id, null, 1);
            if (existing == null) {
                return Result.error("无权限删除该患者");
            }
        }
        patientService.deleteById(id);
        return Result.success("患者删除成功");
    }

    /**
     * 批量导入患者
     * 请求体: { "patients": [ { "name", "gender", "admissionNumber", "phone" }, ... ], "currentDoctorId": 可选 }
     */
    @PostMapping("/import")
    public Result batchImport(@RequestBody Map<String, Object> params) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> list = (List<Map<String, Object>>) params.get("patients");
        Integer currentDoctorId = params.get("currentDoctorId") != null
            ? ((Number) params.get("currentDoctorId")).intValue() : null;

        if (list == null || list.isEmpty()) {
            return Result.error("导入数据不能为空");
        }

        List<Patient> patients = new java.util.ArrayList<>();
        for (Map<String, Object> m : list) {
            Patient p = new Patient();
            p.setName(m.get("name") != null ? m.get("name").toString().trim() : null);
            p.setGender(m.get("gender") != null ? m.get("gender").toString().trim() : null);
            p.setAdmissionNumber(m.get("admissionNumber") != null ? m.get("admissionNumber").toString().trim() : null);
            p.setPhone(m.get("phone") != null ? m.get("phone").toString() : null);
            patients.add(p);
        }

        log.info("批量导入患者，条数: {}, 当前医生ID: {}", patients.size(), currentDoctorId);
        BatchImportResult result = patientService.batchImport(patients, currentDoctorId);
        return Result.success(result);
    }
}
