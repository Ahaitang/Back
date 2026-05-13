package org.hospital.neuroimmune.controller;

import org.hospital.common.audit.AuditLog;
import org.hospital.common.audit.OperationType;
import org.hospital.common.model.Result;
import org.hospital.common.model.LoginRequest;
import org.hospital.common.model.PasswordRequest;
import org.hospital.common.security.SecurityContextHelper;
import org.hospital.common.security.TokenStorage;
import org.hospital.common.security.UserInfo;
import org.hospital.neuroimmune.model.LoginResult;
import org.hospital.neuroimmune.model.RegisterRequest;
import org.hospital.neuroimmune.model.RegisterResult;
import org.hospital.neuroimmune.service.AuthService;
import org.hospital.neuroimmune.service.PatientService;
import org.hospital.neuroimmune.service.DoctorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/neuroimmune")
@CrossOrigin
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthService authService;

    @Autowired
    private PatientService patientService;

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private TokenStorage tokenStorage;

    /**
     * 统一登录接口
     * 管理后台：自动识别 admin 或 doctor（不允许患者登录）
     * 小程序端：需传入 role=patient
     */
    @AuditLog(operation = OperationType.LOGIN, module = "认证", description = "用户登录", logParams = true, logResult = false)
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        logger.info("登录请求: username={}, role={}", request.getUsername(), request.getRole());

        LoginResult result = authService.login(request);
        if (result.isSuccess()) {
            return Result.success(result.toMap());
        }
        return Result.error(401, result.getErrorMessage());
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
    public Result<Void> updateAdminPassword(@PathVariable Long id, @Valid @RequestBody PasswordRequest request) {
        return updatePassword(id, "admin", request);
    }

    /**
     * 修改患者密码
     */
    @AuditLog(operation = OperationType.CHANGE_PASSWORD, module = "认证", description = "修改患者密码", logParams = false)
    @PutMapping("/patients/{id}/password")
    public Result<Void> updatePatientPassword(@PathVariable Long id, @Valid @RequestBody PasswordRequest request) {
        return updatePassword(id, "patient", request);
    }

    /**
     * 修改医生密码
     */
    @AuditLog(operation = OperationType.CHANGE_PASSWORD, module = "认证", description = "修改医生密码", logParams = false)
    @PutMapping("/doctors/{id}/password")
    public Result<Void> updateDoctorPassword(@PathVariable Long id, @Valid @RequestBody PasswordRequest request) {
        return updatePassword(id, "doctor", request);
    }

    private Result<Void> updatePassword(Long id, String role, PasswordRequest request) {
        try {
            authService.updatePasswordAndRemoveToken(id, role, request, SecurityContextHelper.getCurrentUser());
            return Result.success();
        } catch (SecurityException e) {
            return Result.error(403, e.getMessage());
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage());
        }
    }

    /**
     * 患者注册接口
     */
    @PostMapping("/register")
    public Result<Map<String, Object>> register(@Valid @RequestBody RegisterRequest request) {
        logger.info("注册请求: phone={}, doctorId={}", request.getPhone(), request.getDoctorId());

        RegisterResult result = authService.register(request);
        if (result.isSuccess()) {
            Map<String, Object> data = new HashMap<>();
            data.put("patientId", result.getPatientId());
            data.put("status", result.getStatus());
            return Result.success(data, "注册成功，等待医生确认");
        }
        return Result.error(400, result.getErrorMessage());
    }

    /**
     * 检查手机号是否已注册
     */
    @GetMapping("/register/check-phone")
    public Result<Map<String, Object>> checkPhone(@RequestParam String phone) {
        boolean exists = authService.checkPhoneExists(phone);
        Map<String, Object> data = new HashMap<>();
        data.put("exists", exists);
        return Result.success(data);
    }
}
