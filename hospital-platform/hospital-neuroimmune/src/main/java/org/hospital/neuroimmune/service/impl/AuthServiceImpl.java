package org.hospital.neuroimmune.service.impl;

import org.hospital.common.model.LoginRequest;
import org.hospital.common.security.JwtUtil;
import org.hospital.common.security.TokenStorage;
import org.hospital.common.security.UserInfo;
import org.hospital.neuroimmune.entity.Doctor;
import org.hospital.neuroimmune.entity.Patient;
import org.hospital.neuroimmune.model.LoginResult;
import org.hospital.neuroimmune.service.AuthService;
import org.hospital.neuroimmune.service.DoctorRoleService;
import org.hospital.neuroimmune.service.DoctorService;
import org.hospital.neuroimmune.service.PatientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 认证服务实现类
 * 处理登录编排逻辑：角色识别、认证、Token生成
 */
@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private DoctorRoleService doctorRoleService;

    @Autowired
    private PatientService patientService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private TokenStorage tokenStorage;

    @Override
    public LoginResult login(LoginRequest request) {
        String role = request.getRole();
        String username = request.getUsername();

        // 自动判断角色：管理后台登录时检查 doctor 表
        if (role == null || role.isEmpty()) {
            RoleDetectionResult detection = detectRoleFromDoctor(username);
            if (detection == null) {
                logger.warn("未识别的用户类型: {}", username);
                return LoginResult.fail("用户名或密码错误");
            }
            role = detection.role;
        }

        // 根据角色执行登录
        return switch (role) {
            case "admin" -> handleAdminLogin(request);
            case "doctor" -> handleDoctorLogin(request);
            case "patient" -> handlePatientLogin(request);
            default -> {
                logger.warn("无效的角色: {}", role);
                yield LoginResult.fail("无效的角色类型");
            }
        };
    }

    @Override
    public void updatePasswordAndRemoveToken(Long userId, String role, String newPassword) {
        switch (role) {
            case "admin", "doctor" -> {
                doctorService.updatePassword(userId, newPassword);
                tokenStorage.removeToken(new UserInfo(userId, null, role, "neuroimmune"));
            }
            case "patient" -> {
                patientService.updatePassword(userId, newPassword);
                tokenStorage.removeToken(new UserInfo(userId, null, "patient", "neuroimmune"));
            }
        }
    }

    /**
     * 从医生表检测角色
     */
    private RoleDetectionResult detectRoleFromDoctor(String username) {
        Doctor doctor = doctorService.getByPhone(username);
        if (doctor == null) {
            return null;
        }

        List<String> roles = doctorRoleService.getRoleCodesByDoctorId(doctor.getId());
        if (roles.contains("ADMIN")) {
            logger.info("自动识别为管理员: {} (level={})", username, doctor.getLevel());
            return new RoleDetectionResult("admin", doctor);
        } else if (roles.contains("DOCTOR")) {
            logger.info("自动识别为医生: {}", username);
            return new RoleDetectionResult("doctor", doctor);
        } else {
            logger.warn("用户无有效角色: {}", username);
            return null;
        }
    }

    /**
     * 处理管理员登录
     */
    private LoginResult handleAdminLogin(LoginRequest request) {
        Doctor doctor = doctorService.login(request);
        if (doctor == null) {
            return LoginResult.fail("用户名或密码错误");
        }

        List<String> roles = doctorRoleService.getRoleCodesByDoctorId(doctor.getId());
        if (!roles.contains("ADMIN")) {
            return LoginResult.fail("用户名或密码错误");
        }

        return createSuccessLoginResult(doctor, "admin");
    }

    /**
     * 处理医生登录
     */
    private LoginResult handleDoctorLogin(LoginRequest request) {
        Doctor doctor = doctorService.login(request);
        if (doctor == null) {
            return LoginResult.fail("用户名或密码错误");
        }

        List<String> roles = doctorRoleService.getRoleCodesByDoctorId(doctor.getId());
        if (!roles.contains("DOCTOR")) {
            return LoginResult.fail("用户名或密码错误");
        }

        return createSuccessLoginResult(doctor, "doctor");
    }

    /**
     * 处理患者登录
     */
    private LoginResult handlePatientLogin(LoginRequest request) {
        Patient patient = patientService.login(request);
        if (patient == null) {
            return LoginResult.fail("用户名或密码错误");
        }

        UserInfo userInfo = new UserInfo(patient.getId(), patient.getName(), "patient", "neuroimmune");
        String token = jwtUtil.generateToken(userInfo);
        tokenStorage.storeToken(userInfo, token);

        return LoginResult.ok(token, patient, "patient");
    }

    /**
     * 创建成功的登录结果（针对Doctor用户）
     */
    private LoginResult createSuccessLoginResult(Doctor doctor, String role) {
        UserInfo userInfo = new UserInfo(doctor.getId(), doctor.getPhone(), role, "neuroimmune");
        String token = jwtUtil.generateToken(userInfo);
        tokenStorage.storeToken(userInfo, token);

        return LoginResult.ok(token, doctor, role);
    }

    /**
     * 角色检测结果内部类
     */
    private record RoleDetectionResult(String role, Doctor doctor) {}
}