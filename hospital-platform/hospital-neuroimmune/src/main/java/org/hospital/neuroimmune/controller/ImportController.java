package org.hospital.neuroimmune.controller;

import org.hospital.common.model.Result;
import org.hospital.common.model.ImportResult;
import org.hospital.neuroimmune.entity.Doctor;
import org.hospital.neuroimmune.entity.Patient;
import org.hospital.neuroimmune.service.DoctorService;
import org.hospital.neuroimmune.service.PatientService;
import org.hospital.neuroimmune.service.PatientDoctorRelationService;
import org.hospital.common.util.ExcelUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.security.SecureRandom;

@RestController
@RequestMapping("/api/v1/neuroimmune/import")
@CrossOrigin
public class ImportController {

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private PatientService patientService;

    @Autowired
    private PatientDoctorRelationService relationService;

    /**
     * 下载医生导入模板
     */
    @GetMapping("/doctor/template")
    public ResponseEntity<byte[]> downloadDoctorTemplate() throws IOException {
        byte[] data = ExcelUtil.generateDoctorTemplate();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=doctor_template.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(data);
    }

    /**
     * 下载患者导入模板
     */
    @GetMapping("/patient/template")
    public ResponseEntity<byte[]> downloadPatientTemplate() throws IOException {
        byte[] data = ExcelUtil.generatePatientTemplate();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=patient_template.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(data);
    }

    /**
     * 导入医生
     */
    @PostMapping("/doctor")
    public Result<ImportResult> importDoctors(@RequestParam("file") MultipartFile file) {
        ImportResult result = new ImportResult();

        try {
            List<List<String>> data = ExcelUtil.readExcel(file);
            if (data.isEmpty()) {
                return Result.error("文件为空");
            }

            // 跳过表头
            for (int i = 1; i < data.size(); i++) {
                List<String> row = data.get(i);
                result.setTotal(result.getTotal() + 1);

                try {
                    // 验证必填字段
                    if (row.size() < 5) {
                        result.addError(i + 1, "数据列数不足");
                        continue;
                    }

                    String name = row.get(0).trim();
                    String title = row.size() > 1 ? row.get(1).trim() : "";
                    String department = row.size() > 2 ? row.get(2).trim() : "";
                    String hospital = row.size() > 3 ? row.get(3).trim() : "";
                    String phone = row.size() > 4 ? row.get(4).trim() : "";
                    String password = row.size() > 5 && !row.get(5).trim().isEmpty()
                        ? row.get(5).trim()
                        : generateRandomPassword();  // 生成随机密码而非硬编码

                    // 验证必填字段
                    if (name.isEmpty()) {
                        result.addError(i + 1, "姓名不能为空");
                        continue;
                    }
                    if (department.isEmpty()) {
                        result.addError(i + 1, "科室不能为空");
                        continue;
                    }
                    if (phone.isEmpty()) {
                        result.addError(i + 1, "手机号不能为空");
                        continue;
                    }

                    // 检查手机号是否已存在
                    Doctor existing = doctorService.getByPhone(phone);
                    if (existing != null) {
                        result.addError(i + 1, "手机号 " + phone + " 已存在");
                        continue;
                    }

                    // 创建医生
                    Doctor doctor = new Doctor();
                    doctor.setName(name);
                    doctor.setTitle(title);
                    doctor.setDepartment(department);
                    doctor.setHospital(hospital);
                    doctor.setPhone(phone);
                    doctor.setPassword(password);

                    doctorService.save(doctor);
                    result.success();

                } catch (Exception e) {
                    result.addError(i + 1, e.getMessage());
                }
            }

            return Result.success(result);

        } catch (IOException e) {
            return Result.error("读取文件失败: " + e.getMessage());
        }
    }

    /**
     * 导入患者
     */
    @PostMapping("/patient")
    public Result<ImportResult> importPatients(@RequestParam("file") MultipartFile file) {
        ImportResult result = new ImportResult();

        try {
            List<List<String>> data = ExcelUtil.readExcel(file);
            if (data.isEmpty()) {
                return Result.error("文件为空");
            }

            // 跳过表头
            for (int i = 1; i < data.size(); i++) {
                List<String> row = data.get(i);
                result.setTotal(result.getTotal() + 1);

                try {
                    // 验证必填字段
                    if (row.size() < 7) {
                        result.addError(i + 1, "数据列数不足");
                        continue;
                    }

                    String name = row.get(0).trim();
                    String gender = row.size() > 1 ? row.get(1).trim() : "";
                    String ageStr = row.size() > 2 ? row.get(2).trim() : "";
                    String phone = row.size() > 3 ? row.get(3).trim() : "";
                    String idCard = row.size() > 4 ? row.get(4).trim() : "";
                    String password = row.size() > 5 && !row.get(5).trim().isEmpty()
                        ? row.get(5).trim()
                        : generateRandomPassword();  // 生成随机密码而非硬编码
                    String doctorPhone = row.size() > 6 ? row.get(6).trim() : "";

                    // 验证必填字段
                    if (name.isEmpty()) {
                        result.addError(i + 1, "姓名不能为空");
                        continue;
                    }
                    if (gender.isEmpty()) {
                        result.addError(i + 1, "性别不能为空");
                        continue;
                    }
                    if (phone.isEmpty()) {
                        result.addError(i + 1, "手机号不能为空");
                        continue;
                    }
                    if (doctorPhone.isEmpty()) {
                        result.addError(i + 1, "医生手机号不能为空");
                        continue;
                    }

                    // 解析年龄
                    Integer age = 0;
                    try {
                        age = Integer.parseInt(ageStr);
                    } catch (NumberFormatException e) {
                        // 年龄格式错误，使用默认值
                    }

                    // 查找医生
                    Doctor doctor = doctorService.getByPhone(doctorPhone);
                    if (doctor == null) {
                        result.addError(i + 1, "医生手机号 " + doctorPhone + " 不存在");
                        continue;
                    }

                    // 检查患者手机号是否已存在
                    Patient existing = patientService.getByPhone(phone);
                    if (existing != null) {
                        result.addError(i + 1, "手机号 " + phone + " 已存在");
                        continue;
                    }

                    // 创建患者
                    Patient patient = new Patient();
                    patient.setName(name);
                    patient.setGender(gender);
                    patient.setAge(age);
                    patient.setPhone(phone);
                    patient.setIdCard(idCard);
                    patient.setPassword(password);
                    patient.setHasFollowUp(false);
                    patient.setIsRealAuth(false);

                    patientService.save(patient);

                    // 绑定医生
                    relationService.bindDoctor(patient.getId(), doctor.getId(), "system", "批量导入");

                    result.success();

                } catch (Exception e) {
                    result.addError(i + 1, e.getMessage());
                }
            }

            return Result.success(result);

        } catch (IOException e) {
            return Result.error("读取文件失败: " + e.getMessage());
        }
    }

    /**
     * 生成随机密码（8位，包含字母和数字）
     */
    private String generateRandomPassword() {
        SecureRandom random = new SecureRandom();
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}