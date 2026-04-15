package org.hospital.qmg.controller;

import lombok.extern.slf4j.Slf4j;
import org.hospital.common.audit.AuditLog;
import org.hospital.common.audit.OperationType;
import org.hospital.qmg.entity.Doctor;
import org.hospital.common.model.Result;
import org.hospital.common.security.JwtUtil;
import org.hospital.common.security.TokenStorage;
import org.hospital.common.security.UserInfo;
import org.hospital.qmg.service.DoctorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 医生Controller
 * 全部使用POST请求
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/qmg/doctor")
@CrossOrigin(origins = "*")
public class QmgDoctorController {

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private TokenStorage tokenStorage;

    /**
     * 医生登录
     */
    @AuditLog(operation = OperationType.LOGIN, module = "认证", description = "医生登录", logParams = true, logResult = false)
    @PostMapping("/login")
    public Result login(@RequestBody Map<String, String> params) {
        String username = params.get("username");
        String password = params.get("password");

        // 确保用户名和密码都被 trim
        if (username != null) {
            username = username.trim();
        }
        if (password != null) {
            password = password.trim();
        }

        log.info("医生登录请求: username={}, passwordLength={}", username, password != null ? password.length() : 0);

        if (username == null || username.isEmpty()) {
            return Result.error("用户名不能为空");
        }

        if (password == null || password.isEmpty()) {
            return Result.error("密码不能为空");
        }

        try {
            Doctor doctor = doctorService.login(username, password);

            if (doctor != null) {
                // 生成 JWT Token
                UserInfo userInfo = new UserInfo(
                    doctor.getId().longValue(),
                    doctor.getUsername(),
                    doctor.getLevel() == 0 ? "admin" : (doctor.getLevel() == 1 ? "manager" : "doctor"),
                    "qmg"
                );
                String token = jwtUtil.generateToken(userInfo);
                tokenStorage.storeToken(userInfo, token);

                // 返回医生信息（不包含密码）
                Map<String, Object> data = new HashMap<>();
                data.put("token", token);
                data.put("id", doctor.getId());
                data.put("username", doctor.getUsername());
                if (doctor.getEmployeeNumber() != null) {
                    data.put("employeeNumber", doctor.getEmployeeNumber());
                }
                if (doctor.getLevel() != null) {
                    data.put("level", doctor.getLevel());
                    data.put("role", userInfo.getRole());
                }

                log.info("医生登录成功: username={}, id={}, level={}", username, doctor.getId(), doctor.getLevel());
                return Result.success(data);
            } else {
                log.warn("医生登录失败: username={}, 原因：用户名或密码错误", username);
                return Result.error("用户名或密码错误");
            }
        } catch (Exception e) {
            log.error("医生登录异常: username={}, error={}", username, e.getMessage(), e);
            return Result.error("登录失败：" + e.getMessage());
        }
    }

    /**
     * 登出接口（踢下线）
     */
    @AuditLog(operation = OperationType.LOGOUT, module = "认证", description = "用户登出")
    @PostMapping("/logout")
    public Result logout(@RequestBody Map<String, Object> params) {
        Integer id = params.get("id") != null ? ((Number) params.get("id")).intValue() : null;
        Integer level = params.get("level") != null ? ((Number) params.get("level")).intValue() : null;

        if (id != null && level != null) {
            String role = level == 0 ? "admin" : (level == 1 ? "manager" : "doctor");
            tokenStorage.removeToken(new UserInfo(id.longValue(), null, role, "qmg"));
            log.info("用户登出成功: id={}, role={}", id, role);
        }
        return Result.success("登出成功");
    }

    /**
     * 根据用户名查询医生
     */
    @PostMapping("/getByUsername")
    public Result getByUsername(@RequestBody Map<String, String> params) {
        String username = params.get("username");
        log.info("根据用户名查询医生: {}", username);

        Doctor doctor = doctorService.findByUsername(username);
        if (doctor != null) {
            // 不返回密码
            doctor.setPassword(null);
            return Result.success(doctor);
        }
        return Result.error("医生不存在");
    }


    /**
     * 获取所有医生列表（需要权限验证）
     */
    @PostMapping("/list")
    public Result list(@RequestBody Map<String, Object> params) {
        // 从请求中获取当前登录用户的权限等级
        Integer currentUserLevel = params.get("currentUserLevel") != null
            ? ((Number) params.get("currentUserLevel")).intValue()
            : null;

        log.info("获取医生列表请求，当前用户权限等级: {}", currentUserLevel);

        // 权限检查：只有权限等级0和1的用户可以查看
        if (currentUserLevel == null || (currentUserLevel != 0 && currentUserLevel != 1)) {
            log.warn("权限不足，无法查看医生列表，当前用户权限等级: {}", currentUserLevel);
            return Result.error("权限不足，无法查看医生列表");
        }

        try {
            List<Doctor> doctors = doctorService.findAll();
            // 不返回密码
            doctors.forEach(doctor -> doctor.setPassword(null));
            log.info("获取医生列表成功，数量: {}", doctors.size());
            return Result.success(doctors);
        } catch (Exception e) {
            log.error("获取医生列表失败: {}", e.getMessage(), e);
            return Result.error("获取医生列表失败：" + e.getMessage());
        }
    }

    /**
     * 更新医生信息（需要权限验证）
     */
    @AuditLog(operation = OperationType.UPDATE, module = "医生管理", description = "更新医生信息", logParams = false, logResult = false)
    @PostMapping("/update")
    public Result update(@RequestBody Map<String, Object> params) {
        // 从请求中获取当前登录用户的权限等级
        Integer currentUserLevel = params.get("currentUserLevel") != null
            ? ((Number) params.get("currentUserLevel")).intValue()
            : null;

        Integer id = params.get("id") != null ? ((Number) params.get("id")).intValue() : null;
        String employeeNumber = (String) params.get("employeeNumber");
        String username = (String) params.get("username");
        String password = (String) params.get("password");
        Integer level = params.get("level") != null ? ((Number) params.get("level")).intValue() : null;

        log.info("更新医生信息请求: id={}, employeeNumber={}, username={}, currentUserLevel={}",
            id, employeeNumber, username, currentUserLevel);

        if (id == null) {
            return Result.error("医生ID不能为空");
        }

        try {
            Doctor existingDoctor = doctorService.findById(id);
            if (existingDoctor == null) {
                return Result.error("医生不存在");
            }

            // 如果修改了工号，检查新工号是否已存在
            if (employeeNumber != null && !employeeNumber.trim().isEmpty()
                && !employeeNumber.trim().equals(existingDoctor.getEmployeeNumber())) {
                Doctor doctorWithSameNumber = doctorService.findByEmployeeNumber(employeeNumber.trim());
                if (doctorWithSameNumber != null && !doctorWithSameNumber.getId().equals(id)) {
                    return Result.error("工号已存在");
                }
            }

            // 权限检查：只有权限等级0的用户可以修改权限等级
            if (level != null && !level.equals(existingDoctor.getLevel())) {
                if (currentUserLevel == null || currentUserLevel != 0) {
                    log.warn("权限不足，无法修改权限等级，当前用户权限等级: {}", currentUserLevel);
                    return Result.error("权限不足，只有超级管理员可以修改权限等级");
                }
            }

            // 更新信息
            if (employeeNumber != null && !employeeNumber.trim().isEmpty()) {
                existingDoctor.setEmployeeNumber(employeeNumber.trim());
            }
            if (username != null) {
                existingDoctor.setUsername(username.trim());
            }
            // 仅当请求中明确传入新密码时才更新密码
            if (password != null && !password.trim().isEmpty()) {
                existingDoctor.setPassword(password.trim());
                // 踢下线（密码修改后需要重新登录）
                String oldRole = existingDoctor.getLevel() == 0 ? "admin" :
                    (existingDoctor.getLevel() == 1 ? "manager" : "doctor");
                tokenStorage.removeToken(new UserInfo(id.longValue(), null, oldRole, "qmg"));
            } else {
                existingDoctor.setPassword(null);
            }
            if (level != null && currentUserLevel != null && currentUserLevel == 0) {
                existingDoctor.setLevel(level);
            }

            doctorService.update(existingDoctor);

            log.info("更新医生信息成功: id={}, employeeNumber={}, username={}",
                id, existingDoctor.getEmployeeNumber(), existingDoctor.getUsername());
            return Result.success("更新成功");

        } catch (Exception e) {
            log.error("更新医生信息失败: {}", e.getMessage(), e);
            return Result.error("更新失败：" + e.getMessage());
        }
    }

    /**
     * 注册医生（支持单个和批量注册）
     */
    @AuditLog(operation = OperationType.CREATE, module = "医生管理", description = "注册医生")
    @PostMapping("/register")
    public Result register(@RequestBody Map<String, Object> params) {
        // 权限检查：只有权限等级0和1的用户可以新增
        Integer currentUserLevel = params.get("currentUserLevel") != null
            ? ((Number) params.get("currentUserLevel")).intValue() : null;
        if (currentUserLevel == null || (currentUserLevel != 0 && currentUserLevel != 1)) {
            log.warn("权限不足，无法新增医生，当前用户权限等级: {}", currentUserLevel);
            return Result.error("权限不足，无法新增医生");
        }

        // 统一转换为列表格式处理
        @SuppressWarnings("unchecked")
        List<Map<String, String>> doctorsList = (List<Map<String, String>>) params.get("doctors");
        if (doctorsList == null || doctorsList.isEmpty()) {
            // 单个注册转换为列表格式
            String employeeNumber = (String) params.get("employeeNumber");
            String username = (String) params.get("username");
            String password = (String) params.get("password");
            if (employeeNumber == null || employeeNumber.trim().isEmpty()) {
                return Result.error("工号不能为空");
            }
            if (username == null || username.trim().isEmpty()) {
                return Result.error("用户名不能为空");
            }
            if (password == null || password.trim().isEmpty()) {
                return Result.error("密码不能为空");
            }
            Map<String, String> doctorMap = new java.util.HashMap<>();
            doctorMap.put("employeeNumber", employeeNumber);
            doctorMap.put("username", username);
            doctorMap.put("password", password);
            doctorsList = java.util.Collections.singletonList(doctorMap);
        }

        boolean isSingle = doctorsList.size() == 1;
        log.info("注册医生请求，数量: {}", doctorsList.size());

        try {
            List<Doctor> doctors = new java.util.ArrayList<>();
            for (Map<String, String> doctorMap : doctorsList) {
                String employeeNumber = doctorMap.get("employeeNumber");
                String username = doctorMap.get("username");
                String password = doctorMap.get("password");

                if (employeeNumber != null) employeeNumber = employeeNumber.trim();
                if (username != null) username = username.trim();
                if (password != null) password = password.trim();

                if (employeeNumber == null || employeeNumber.isEmpty()) {
                    return Result.error("工号不能为空");
                }
                if (username == null || username.isEmpty()) {
                    return Result.error("用户名不能为空");
                }
                if (password == null || password.isEmpty()) {
                    return Result.error("密码不能为空，工号: " + employeeNumber);
                }

                Doctor existing = doctorService.findByEmployeeNumber(employeeNumber);
                if (existing != null) {
                    if (isSingle) return Result.error("工号已存在");
                    log.warn("工号已存在，跳过: {}", employeeNumber);
                    continue;
                }

                Doctor doctor = new Doctor();
                doctor.setEmployeeNumber(employeeNumber);
                doctor.setUsername(username);
                doctor.setPassword(password);
                doctor.setLevel(2);
                doctors.add(doctor);
            }

            if (doctors.isEmpty()) {
                return Result.error("所有医生账号都已存在");
            }

            int count = doctorService.batchRegister(doctors);

            if (isSingle) {
                log.info("注册医生成功: employeeNumber={}, username={}",
                    doctors.get(0).getEmployeeNumber(), doctors.get(0).getUsername());
                return Result.success("注册成功");
            } else {
                Map<String, Object> data = new HashMap<>();
                data.put("successCount", count);
                data.put("totalCount", doctorsList.size());
                return Result.success(data);
            }
        } catch (Exception e) {
            log.error("注册医生异常: {}", e.getMessage(), e);
            return Result.error("注册失败：" + e.getMessage());
        }
    }
}