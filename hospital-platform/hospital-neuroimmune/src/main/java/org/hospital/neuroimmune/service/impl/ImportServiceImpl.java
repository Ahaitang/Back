package org.hospital.neuroimmune.service.impl;

import org.hospital.common.model.ImportResult;
import org.hospital.common.util.ExcelUtil;
import org.hospital.neuroimmune.entity.Doctor;
import org.hospital.neuroimmune.entity.Patient;
import org.hospital.neuroimmune.service.DoctorService;
import org.hospital.neuroimmune.service.PatientService;
import org.hospital.neuroimmune.service.PatientDoctorRelationService;
import org.hospital.neuroimmune.service.ImportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 导入服务实现
 */
@Service
public class ImportServiceImpl implements ImportService {

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private PatientService patientService;

    @Autowired
    private PatientDoctorRelationService relationService;

    @Override
    public ImportResult importDoctors(MultipartFile file) throws IOException {
        ImportResult result = new ImportResult();

        List<List<String>> data = ExcelUtil.readExcel(file);
        if (data.isEmpty()) {
            result.addError(0, "文件为空");
            return result;
        }

        // 跳过表头
        for (int i = 1; i < data.size(); i++) {
            List<String> row = data.get(i);
            result.setTotal(result.getTotal() + 1);

            try {
                String error = validateAndImportDoctor(row, i + 1);
                if (error != null) {
                    result.addError(i + 1, error);
                } else {
                    result.success();
                }
            } catch (Exception e) {
                result.addError(i + 1, e.getMessage());
            }
        }

        return result;
    }

    @Override
    public ImportResult importPatients(MultipartFile file) throws IOException {
        ImportResult result = new ImportResult();

        List<List<String>> data = ExcelUtil.readExcel(file);
        if (data.isEmpty()) {
            result.addError(0, "文件为空");
            return result;
        }

        // 跳过表头
        for (int i = 1; i < data.size(); i++) {
            List<String> row = data.get(i);
            result.setTotal(result.getTotal() + 1);

            try {
                String error = validateAndImportPatient(row, i + 1);
                if (error != null) {
                    result.addError(i + 1, error);
                } else {
                    result.success();
                }
            } catch (Exception e) {
                result.addError(i + 1, e.getMessage());
            }
        }

        return result;
    }

    /**
     * 验证并导入医生
     */
    private String validateAndImportDoctor(List<String> row, int rowNum) {
        // 验证列数
        if (row.size() < 6) {
            return "数据列数不足";
        }

        String name = row.get(0).trim();
        String title = row.size() > 1 ? row.get(1).trim() : "";
        String department = row.size() > 2 ? row.get(2).trim() : "";
        String hospital = row.size() > 3 ? row.get(3).trim() : "";
        String phone = row.size() > 4 ? row.get(4).trim() : "";
        String password = row.size() > 5 ? row.get(5).trim() : "";

        // 验证必填字段
        if (name.isEmpty()) return "姓名不能为空";
        if (department.isEmpty()) return "科室不能为空";
        if (phone.isEmpty()) return "手机号不能为空";
        if (password.isEmpty()) return "密码不能为空";

        // 检查手机号是否已存在
        Doctor existing = doctorService.getByPhone(phone);
        if (existing != null) {
            return "手机号 " + phone + " 已存在";
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
        return null; // 成功
    }

    /**
     * 验证并导入患者
     */
    private String validateAndImportPatient(List<String> row, int rowNum) {
        // 验证列数
        if (row.size() < 7) {
            return "数据列数不足";
        }

        String name = row.get(0).trim();
        String gender = row.size() > 1 ? row.get(1).trim() : "";
        String birthDateStr = row.size() > 2 ? row.get(2).trim() : "";
        String phone = row.size() > 3 ? row.get(3).trim() : "";
        String idCard = row.size() > 4 ? row.get(4).trim() : "";
        String password = row.size() > 5 ? row.get(5).trim() : "";
        String doctorPhone = row.size() > 6 ? row.get(6).trim() : "";

        // 验证必填字段
        if (name.isEmpty()) return "姓名不能为空";
        if (gender.isEmpty()) return "性别不能为空";
        if (birthDateStr.isEmpty()) return "出生日期不能为空";
        if (phone.isEmpty()) return "手机号不能为空";
        if (password.isEmpty()) return "密码不能为空";
        if (doctorPhone.isEmpty()) return "医生手机号不能为空";

        // 解析出生日期
        LocalDateTime birthDate;
        try {
            birthDate = LocalDate.parse(birthDateStr).atStartOfDay();
        } catch (Exception e) {
            return "出生日期格式错误，应为 yyyy-MM-dd";
        }

        // 查找医生
        Doctor doctor = doctorService.getByPhone(doctorPhone);
        if (doctor == null) {
            return "医生手机号 " + doctorPhone + " 不存在";
        }

        // 检查患者手机号是否已存在
        Patient existing = patientService.getByPhone(phone);
        if (existing != null) {
            return "手机号 " + phone + " 已存在";
        }

        // 创建患者
        Patient patient = new Patient();
        patient.setName(name);
        patient.setGender(gender);
        patient.setBirthDate(birthDate);
        patient.setPhone(phone);
        patient.setIdCard(idCard);
        patient.setPassword(password);
        patient.setHasFollowUp(false);
        patient.setIsRealAuth(false);

        patientService.save(patient);

        // 绑定医生
        relationService.bindDoctor(patient.getId(), doctor.getId(), "system", "批量导入");

        return null; // 成功
    }
}