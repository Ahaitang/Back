package org.hospital.neuroimmune.service.impl;

import org.hospital.common.model.LoginRequest;
import org.hospital.common.model.PasswordRequest;
import org.hospital.common.security.JwtUtil;
import org.hospital.common.security.TokenStorage;
import org.hospital.common.security.UserInfo;
import org.hospital.common.util.PasswordUtil;
import org.hospital.neuroimmune.entity.Doctor;
import org.hospital.neuroimmune.entity.Patient;
import org.hospital.neuroimmune.entity.PatientDoctorRelation;
import org.hospital.neuroimmune.model.LoginResult;
import org.hospital.neuroimmune.model.RegisterRequest;
import org.hospital.neuroimmune.model.RegisterResult;
import org.hospital.neuroimmune.service.AuthService;
import org.hospital.neuroimmune.service.DoctorRoleService;
import org.hospital.neuroimmune.service.DoctorService;
import org.hospital.neuroimmune.service.PatientDoctorRelationService;
import org.hospital.neuroimmune.service.PatientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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

    @Autowired
    private PatientDoctorRelationService relationService;

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
    public void updatePasswordAndRemoveToken(Long targetUserId, String targetRole, PasswordRequest request, UserInfo currentUser) {
        if (targetUserId == null || targetRole == null || request == null || currentUser == null) {
            throw new SecurityException("用户信息获取失败");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new IllegalArgumentException("新密码不能为空");
        }

        String role = targetRole.toLowerCase();
        boolean selfChange = targetUserId.equals(currentUser.getUserId())
                && role.equalsIgnoreCase(currentUser.getRole());
        if (selfChange) {
            verifyOldPassword(targetUserId, role, request.getOldPassword());
        } else {
            verifyAdminPasswordChangePermission(currentUser, targetUserId, role);
        }

        switch (role) {
            case "admin", "doctor" -> {
                doctorService.updatePassword(targetUserId, request.getPassword());
                tokenStorage.removeToken(new UserInfo(targetUserId, null, role, "neuroimmune"));
            }
            case "patient" -> {
                patientService.updatePassword(targetUserId, request.getPassword());
                tokenStorage.removeToken(new UserInfo(targetUserId, null, "patient", "neuroimmune"));
            }
            default -> throw new IllegalArgumentException("无效的角色类型");
        }
    }

    private void verifyOldPassword(Long userId, String role, String oldPassword) {
        if (oldPassword == null || oldPassword.isBlank()) {
            throw new IllegalArgumentException("请输入原密码");
        }
        String storedPassword = switch (role) {
            case "admin", "doctor" -> {
                Doctor doctor = doctorService.getById(userId);
                yield doctor != null ? doctor.getPassword() : null;
            }
            case "patient" -> {
                Patient patient = patientService.getById(userId);
                yield patient != null ? patient.getPassword() : null;
            }
            default -> null;
        };
        if (storedPassword == null || !PasswordUtil.matches(oldPassword, storedPassword)) {
            throw new IllegalArgumentException("原密码错误");
        }
    }

    private void verifyAdminPasswordChangePermission(UserInfo currentUser, Long targetUserId, String targetRole) {
        if (!"admin".equalsIgnoreCase(currentUser.getRole())) {
            throw new SecurityException("无权修改其他用户密码");
        }

        if ("patient".equalsIgnoreCase(targetRole)) {
            if (patientService.getById(targetUserId) == null) {
                throw new IllegalArgumentException("用户不存在");
            }
            return;
        }

        Doctor currentAdmin = doctorService.getById(currentUser.getUserId());
        Doctor targetUser = doctorService.getById(targetUserId);
        if (currentAdmin == null || targetUser == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        Integer currentLevel = currentAdmin.getLevel() == null ? 999 : currentAdmin.getLevel();
        Integer targetLevel = targetUser.getLevel() == null ? 999 : targetUser.getLevel();
        if (currentLevel >= targetLevel) {
            throw new SecurityException("无权限修改同级或更高等级用户密码");
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

        // 状态校验（Integer类型）
        Integer status = patient.getStatus();
        if (status == null) {
            status = Patient.STATUS_ACTIVE; // 兼容旧数据
        }

        if (status.equals(Patient.STATUS_PENDING)) {
            // 待审核的患者允许登录，登录后可查看审核状态
            UserInfo userInfo = new UserInfo(patient.getId(), patient.getName(), "patient", "neuroimmune");
            String token = jwtUtil.generateToken(userInfo);
            tokenStorage.storeToken(userInfo, token);
            return LoginResult.ok(token, patient, "patient");
        } else if (status.equals(Patient.STATUS_REJECTED)) {
            // 被拒绝的患者允许登录，登录后可重新选择医生申请绑定
            UserInfo userInfo = new UserInfo(patient.getId(), patient.getName(), "patient", "neuroimmune");
            String token = jwtUtil.generateToken(userInfo);
            tokenStorage.storeToken(userInfo, token);
            return LoginResult.ok(token, patient, "patient");
        } else if (status.equals(Patient.STATUS_INACTIVE)) {
            return LoginResult.fail("账户已被禁用");
        } else if (status.equals(Patient.STATUS_ACTIVE)) {
            // 正常登录
            UserInfo userInfo = new UserInfo(patient.getId(), patient.getName(), "patient", "neuroimmune");
            String token = jwtUtil.generateToken(userInfo);
            tokenStorage.storeToken(userInfo, token);
            return LoginResult.ok(token, patient, "patient");
        } else {
            return LoginResult.fail("账户状态异常");
        }
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

    @Override
    @Transactional
    public RegisterResult register(RegisterRequest request) {
        // 1. 检查手机号是否已存在
        if (checkPhoneExists(request.getPhone())) {
            return RegisterResult.fail("该手机号已注册");
        }

        // 2. 创建患者
        Patient patient = new Patient();
        patient.setPhone(request.getPhone());
        patient.setPassword(PasswordUtil.encode(request.getPassword()));
        patient.setName(request.getName());
        patient.setGender(request.getGender());
        patient.setBirthDate(request.getBirthDate() != null ?
            request.getBirthDate().atStartOfDay() : null);

        // 如果有医生ID，状态为pending等待医生确认；否则直接激活
        if (request.getDoctorId() != null) {
            patient.setStatus(Patient.STATUS_PENDING);
        } else {
            patient.setStatus(Patient.STATUS_ACTIVE);
        }

        // 使用现有的 patientService.save() 方法
        patientService.save(patient);

        // 3. 如果有医生ID，创建待确认的绑定关系
        if (request.getDoctorId() != null) {
            PatientDoctorRelation relation = new PatientDoctorRelation();
            relation.setPatientId(patient.getId());
            relation.setDoctorId(request.getDoctorId());
            relation.setRelationType("primary");
            relation.setStatus(PatientDoctorRelation.STATUS_ACTIVE);
            relation.setBindStatus(PatientDoctorRelation.BIND_STATUS_PENDING);
            relation.setBindMethod("patient");
            relation.setRequestTime(LocalDateTime.now());

            relationService.createPendingRelation(relation);
            logger.info("患者注册成功（待确认）: phone={}, patientId={}, doctorId={}",
                request.getPhone(), patient.getId(), request.getDoctorId());
            return RegisterResult.ok(patient.getId(), "pending");
        } else {
            logger.info("患者注册成功（直接激活）: phone={}, patientId={}",
                request.getPhone(), patient.getId());
            return RegisterResult.ok(patient.getId(), "active");
        }
    }

    @Override
    public boolean checkPhoneExists(String phone) {
        Patient existing = patientService.getByPhone(phone);
        return existing != null;
    }

    /**
     * 角色检测结果内部类
     */
    private record RoleDetectionResult(String role, Doctor doctor) {}
}
