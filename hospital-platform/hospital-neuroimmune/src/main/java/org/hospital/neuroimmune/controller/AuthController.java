package org.hospital.neuroimmune.controller;

import org.hospital.neuroimmune.common.Result;
import org.hospital.neuroimmune.dto.LoginRequest;
import org.hospital.neuroimmune.dto.PasswordRequest;
import org.hospital.neuroimmune.entity.Admin;
import org.hospital.neuroimmune.entity.Patient;
import org.hospital.neuroimmune.entity.Doctor;
import org.hospital.neuroimmune.service.AdminService;
import org.hospital.neuroimmune.service.PatientService;
import org.hospital.neuroimmune.service.DoctorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/neuroimmune")
@CrossOrigin
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AdminService adminService;

    @Autowired
    private PatientService patientService;

    @Autowired
    private DoctorService doctorService;

    /**
     * 统一登录接口
     * role: admin, doctor, patient
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody LoginRequest request) {
        logger.info("登录请求: username={}, role={}", request.getUsername(), request.getRole());

        String role = request.getRole();
        if (role == null || role.isEmpty()) {
            role = "patient"; // 默认患者登录
        }

        Map<String, Object> data = new HashMap<>();

        switch (role) {
            case "admin":
                Admin admin = adminService.login(request);
                logger.info("Admin登录结果: {}", admin != null ? "成功" : "失败");
                if (admin != null) {
                    data.put("token", "admin-token-" + admin.getId());
                    data.put("user", admin);
                    data.put("role", "admin");
                    return Result.success(data);
                }
                break;
            case "doctor":
                Doctor doctor = doctorService.login(request);
                if (doctor != null) {
                    data.put("token", "doctor-token-" + doctor.getId());
                    data.put("user", doctor);
                    data.put("role", "doctor");
                    return Result.success(data);
                }
                break;
            case "patient":
            default:
                Patient patient = patientService.login(request);
                if (patient != null) {
                    data.put("token", "patient-token-" + patient.getId());
                    data.put("user", patient);
                    data.put("role", "patient");
                    return Result.success(data);
                }
                break;
        }

        return Result.error(401, "用户名或密码错误");
    }

    /**
     * 修改管理员密码
     */
    @PutMapping("/admin/{id}/password")
    public Result<Void> updateAdminPassword(@PathVariable Long id, @RequestBody PasswordRequest request) {
        adminService.updatePassword(id, request.getPassword());
        return Result.success();
    }

    /**
     * 修改患者密码
     */
    @PutMapping("/patients/{id}/password")
    public Result<Void> updatePatientPassword(@PathVariable Long id, @RequestBody PasswordRequest request) {
        patientService.updatePassword(id, request.getPassword());
        return Result.success();
    }

    /**
     * 修改医生密码
     */
    @PutMapping("/doctors/{id}/password")
    public Result<Void> updateDoctorPassword(@PathVariable Long id, @RequestBody PasswordRequest request) {
        doctorService.updatePassword(id, request.getPassword());
        return Result.success();
    }
}