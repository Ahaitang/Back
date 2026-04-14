package org.hospital.qmg.controller;

import lombok.extern.slf4j.Slf4j;
import org.hospital.qmg.entity.Doctor;
import org.hospital.qmg.pojo.Result;
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
@RequestMapping("/api/qmg/doctor")
@CrossOrigin(origins = "*")
public class DoctorController {

    @Autowired
    private DoctorService doctorService;

    /**
     * 医生登录
     */
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
                // 返回医生信息（不包含密码）
                Map<String, Object> data = new HashMap<>();
                data.put("id", doctor.getId());
                data.put("username", doctor.getUsername());
                if (doctor.getLevel() != null) {
                    data.put("level", doctor.getLevel());
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
            // 仅当请求中明确传入新密码时才更新密码；未传或为空时置为 null，避免把库里的加密密码再加密一次
            if (password != null && !password.trim().isEmpty()) {
                existingDoctor.setPassword(password.trim()); // Service 层会加密
            } else {
                existingDoctor.setPassword(null); // 不修改密码时不要带出库里的密文，避免被二次加密
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
     * 单个注册会转换为批量注册的特例（只有一个元素的数组）统一处理
     */
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
                
                // 确保所有字段都被 trim
                if (employeeNumber != null) {
                    employeeNumber = employeeNumber.trim();
                }
                if (username != null) {
                    username = username.trim();
                }
                if (password != null) {
                    password = password.trim();
                }
                
                if (employeeNumber == null || employeeNumber.isEmpty()) {
                    return Result.error("工号不能为空");
                }
                if (username == null || username.isEmpty()) {
                    return Result.error("用户名不能为空");
                }
                if (password == null || password.isEmpty()) {
                    return Result.error("密码不能为空，工号: " + employeeNumber);
                }
                
                // 检查工号是否已存在（根据工号判断重复）
                Doctor existing = doctorService.findByEmployeeNumber(employeeNumber);
                if (existing != null) {
                    if (isSingle) {
                        return Result.error("工号已存在");
                    }
                    log.warn("工号已存在，跳过: {}", employeeNumber);
                    continue;
                }
                
                Doctor doctor = new Doctor();
                doctor.setEmployeeNumber(employeeNumber);
                doctor.setUsername(username);
                doctor.setPassword(password); // 密码会在 Service 层加密
                // 权限等级默认为2（普通医生）
                doctor.setLevel(2);
                doctors.add(doctor);
                
                log.debug("准备注册医生: employeeNumber={}, username={}, passwordLength={}", 
                    employeeNumber, username, password.length());
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
                log.info("批量注册医生成功，成功: {}, 总数: {}", count, doctorsList.size());
                return Result.success(data);
            }
        } catch (Exception e) {
            log.error("注册医生异常: {}", e.getMessage(), e);
            return Result.error("注册失败：" + e.getMessage());
        }
    }
}
