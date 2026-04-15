package org.hospital.neuroimmune.controller;

import org.hospital.common.audit.AuditLog;
import org.hospital.common.audit.OperationType;
import org.hospital.common.model.Result;
import org.hospital.common.model.LoginRequest;
import org.hospital.common.model.PasswordRequest;
import org.hospital.common.security.JwtUtil;
import org.hospital.common.security.TokenStorage;
import org.hospital.common.security.UserInfo;
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

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/neuroimmune")
@CrossOrigin
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AdminService adminService;

    @Autowired
    private PatientService patientService;

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private TokenStorage tokenStorage;

    /**
     * 统一登录接口
     * role: admin, doctor, patient
     */
    @AuditLog(operation = OperationType.LOGIN, module = "认证", description = "用户登录", logParams = true, logResult = false)
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        logger.info("登录请求: username={}, role={}", request.getUsername(), request.getRole());

        String role = request.getRole();
        if (role == null || role.isEmpty()) {
            role = "patient"; // 默认患者登录
        }

        Map<String, Object> data = new HashMap<>();
        UserInfo userInfo;
        String token;

        switch (role) {
            case "admin":
                Admin admin = adminService.login(request);
                logger.info("Admin登录结果: {}", admin != null ? "成功" : "失败");
                if (admin != null) {
                    userInfo = new UserInfo(admin.getId(), admin.getUsername(), "admin", "neuroimmune");
                    token = jwtUtil.generateToken(userInfo);
                    tokenStorage.storeToken(userInfo, token);

                    data.put("token", token);
                    data.put("user", admin);
                    data.put("role", "admin");
                    return Result.success(data);
                }
                break;
            case "doctor":
                Doctor doctor = doctorService.login(request);
                if (doctor != null) {
                    userInfo = new UserInfo(doctor.getId(), doctor.getName(), "doctor", "neuroimmune");
                    token = jwtUtil.generateToken(userInfo);
                    tokenStorage.storeToken(userInfo, token);

                    data.put("token", token);
                    data.put("user", doctor);
                    data.put("role", "doctor");
                    return Result.success(data);
                }
                break;
            case "patient":
            default:
                Patient patient = patientService.login(request);
                if (patient != null) {
                    userInfo = new UserInfo(patient.getId(), patient.getName(), "patient", "neuroimmune");
                    token = jwtUtil.generateToken(userInfo);
                    tokenStorage.storeToken(userInfo, token);

                    data.put("token", token);
                    data.put("user", patient);
                    data.put("role", "patient");
                    return Result.success(data);
                }
                break;
        }

        return Result.error(401, "用户名或密码错误");
    }

    /**
     * 登出接口（踢下线）
     */
    @AuditLog(operation = OperationType.LOGOUT, module = "认证", description = "用户登出")
    @PostMapping("/logout")
    public Result<Void> logout() {
        // 从 SecurityContext 获取当前用户
        // 注意：实际使用时需要从请求中获取用户信息
        return Result.success();
    }

    /**
     * 修改管理员密码
     */
    @AuditLog(operation = OperationType.CHANGE_PASSWORD, module = "认证", description = "修改管理员密码", logParams = false)
    @PutMapping("/admin/{id}/password")
    public Result<Void> updateAdminPassword(@PathVariable Long id, @RequestBody PasswordRequest request) {
        adminService.updatePassword(id, request.getPassword());
        // 踢下线
        tokenStorage.removeToken(new UserInfo(id, null, "admin", "neuroimmune"));
        return Result.success();
    }

    /**
     * 修改患者密码
     */
    @AuditLog(operation = OperationType.CHANGE_PASSWORD, module = "认证", description = "修改患者密码", logParams = false)
    @PutMapping("/patients/{id}/password")
    public Result<Void> updatePatientPassword(@PathVariable Long id, @RequestBody PasswordRequest request) {
        patientService.updatePassword(id, request.getPassword());
        // 踢下线
        tokenStorage.removeToken(new UserInfo(id, null, "patient", "neuroimmune"));
        return Result.success();
    }

    /**
     * 修改医生密码
     */
    @AuditLog(operation = OperationType.CHANGE_PASSWORD, module = "认证", description = "修改医生密码", logParams = false)
    @PutMapping("/doctors/{id}/password")
    public Result<Void> updateDoctorPassword(@PathVariable Long id, @RequestBody PasswordRequest request) {
        doctorService.updatePassword(id, request.getPassword());
        // 踢下线
        tokenStorage.removeToken(new UserInfo(id, null, "doctor", "neuroimmune"));
        return Result.success();
    }
}