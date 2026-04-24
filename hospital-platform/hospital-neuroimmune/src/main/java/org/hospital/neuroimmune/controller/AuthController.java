package org.hospital.neuroimmune.controller;

import org.hospital.common.audit.AuditLog;
import org.hospital.common.audit.OperationType;
import org.hospital.common.model.Result;
import org.hospital.common.model.LoginRequest;
import org.hospital.common.model.PasswordRequest;
import org.hospital.common.security.JwtUtil;
import org.hospital.common.security.TokenStorage;
import org.hospital.common.security.UserInfo;
import org.hospital.neuroimmune.entity.Patient;
import org.hospital.neuroimmune.entity.Doctor;
import org.hospital.neuroimmune.service.PatientService;
import org.hospital.neuroimmune.service.DoctorService;
import org.hospital.neuroimmune.service.DoctorRoleService;
import org.hospital.common.util.PasswordUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/neuroimmune")
@CrossOrigin
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private PatientService patientService;

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private DoctorRoleService doctorRoleService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private TokenStorage tokenStorage;

    /**
     * 统一登录接口
     * 管理后台：自动识别 admin 或 doctor（不允许患者登录）
     * 小程序端：需传入 role=patient
     *
     * 角色判断逻辑：
     * - roles 包含 'ADMIN' → 管理员登录
     * - roles 包含 'DOCTOR' → 医生登录
     */
    @AuditLog(operation = OperationType.LOGIN, module = "认证", description = "用户登录", logParams = true, logResult = false)
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        logger.info("登录请求: username={}, role={}", request.getUsername(), request.getRole());

        String role = request.getRole();
        String username = request.getUsername();

        // 自动判断角色：管理后台登录时检查 doctor 表
        if (role == null || role.isEmpty()) {
            Doctor doctor = doctorService.getByPhone(username);
            if (doctor != null) {
                List<String> roles = doctorRoleService.getRoleCodesByDoctorId(doctor.getId());
                if (roles.contains("ADMIN")) {
                    role = "admin";
                    logger.info("自动识别为管理员: {} (level={})", username, doctor.getLevel());
                } else if (roles.contains("DOCTOR")) {
                    role = "doctor";
                    logger.info("自动识别为医生: {}", username);
                } else {
                    logger.warn("用户无有效角色: {}", username);
                    return Result.error(401, "用户名或密码错误");
                }
            } else {
                logger.warn("未识别的用户类型: {}", username);
                return Result.error(401, "用户名或密码错误");
            }
        }

        Map<String, Object> data = new HashMap<>();
        UserInfo userInfo;
        String token;

        switch (role) {
            case "admin":
                Doctor adminDoctor = doctorService.login(request);
                if (adminDoctor != null) {
                    List<String> roles = doctorRoleService.getRoleCodesByDoctorId(adminDoctor.getId());
                    if (roles.contains("ADMIN")) {
                        userInfo = new UserInfo(adminDoctor.getId(), adminDoctor.getPhone(), "admin", "neuroimmune");
                        token = jwtUtil.generateToken(userInfo);
                        tokenStorage.storeToken(userInfo, token);

                        data.put("token", token);
                        data.put("user", adminDoctor);
                        data.put("role", "admin");
                        return Result.success(data);
                    }
                }
                break;
            case "doctor":
                Doctor doctor = doctorService.login(request);
                if (doctor != null) {
                    List<String> roles = doctorRoleService.getRoleCodesByDoctorId(doctor.getId());
                    if (roles.contains("DOCTOR")) {
                        userInfo = new UserInfo(doctor.getId(), doctor.getName(), "doctor", "neuroimmune");
                        token = jwtUtil.generateToken(userInfo);
                        tokenStorage.storeToken(userInfo, token);

                        data.put("token", token);
                        data.put("user", doctor);
                        data.put("role", "doctor");
                        return Result.success(data);
                    }
                }
                break;
            case "patient":
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
            default:
                logger.warn("无效的角色: {}", role);
                return Result.error(401, "无效的角色类型");
        }

        return Result.error(401, "用户名或密码错误");
    }

    /**
     * 登出接口（踢下线）
     */
    @AuditLog(operation = OperationType.LOGOUT, module = "认证", description = "用户登出")
    @PostMapping("/logout")
    public Result<Void> logout() {
        return Result.success();
    }

    /**
     * 修改管理员密码（通过医生表）
     */
    @AuditLog(operation = OperationType.CHANGE_PASSWORD, module = "认证", description = "修改管理员密码", logParams = false)
    @PutMapping("/admin/{id}/password")
    public Result<Void> updateAdminPassword(@PathVariable Long id, @RequestBody PasswordRequest request) {
        doctorService.updatePassword(id, request.getPassword());
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
        tokenStorage.removeToken(new UserInfo(id, null, "doctor", "neuroimmune"));
        return Result.success();
    }
}