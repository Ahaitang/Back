package org.hospital.common.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.hospital.common.entity.SessionLog;
import org.hospital.common.entity.BlackList;
import org.hospital.common.entity.SystemConfig;
import org.hospital.common.model.Result;
import org.hospital.common.model.PageResult;
import org.hospital.common.service.*;
import org.hospital.common.mapper.SystemConfigMapper;
import org.hospital.common.config.SuperAdminConfig;
import org.hospital.common.security.OnlineUser;
import org.hospital.common.security.TokenManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.time.LocalDateTime;

/**
 * 超级管理平台 Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/super-admin")
@CrossOrigin
public class SuperAdminController {

    @Autowired
    private SuperAdminAuthService authService;

    @Autowired
    private TokenManagementService tokenManagementService;

    @Autowired
    private SessionAuditService auditService;

    @Autowired
    private BlacklistService blacklistService;

    @Autowired
    private SystemConfigMapper systemConfigMapper;

    // ========== 认证接口 ==========

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> params) {
        String username = params.get("username");
        String password = params.get("password");
        String token = authService.login(username, password);
        if (token == null) {
            return Result.error(401, "用户名或密码错误");
        }
        SuperAdminConfig.Account account = authService.getAccount(username);
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("username", username);
        result.put("name", account != null ? account.getName() : username);
        return Result.success(result);
    }

    @PostMapping("/logout")
    public Result<Void> logout(@RequestBody Map<String, String> params) {
        String username = params.get("username");
        if (username != null) {
            authService.logout(username);
        }
        return Result.success();
    }

    // ========== 在线用户管理 ==========

    @PostMapping("/online/list")
    public Result<List<OnlineUser>> getOnlineUsers(@RequestBody(required = false) Map<String, String> params) {
        String module = params != null ? params.get("module") : null;
        String role = params != null ? params.get("role") : null;

        List<OnlineUser> users;
        if (module != null && role != null) {
            users = tokenManagementService.getOnlineUsersByRole(module, role);
        } else if (module != null) {
            users = tokenManagementService.getOnlineUsers(module);
        } else {
            // 获取所有模块的在线用户
            List<OnlineUser> qmgUsers = tokenManagementService.getOnlineUsers("qmg");
            List<OnlineUser> neuroUsers = tokenManagementService.getOnlineUsers("neuroimmune");
            users = new java.util.ArrayList<>();
            users.addAll(qmgUsers);
            users.addAll(neuroUsers);
        }

        return Result.success(users);
    }

    @PostMapping("/online/count")
    public Result<Map<String, Object>> countOnline() {
        long qmgCount = tokenManagementService.countOnlineUsers("qmg");
        long neuroCount = tokenManagementService.countOnlineUsers("neuroimmune");
        Map<String, Object> result = new HashMap<>();
        result.put("total", qmgCount + neuroCount);
        result.put("qmg", qmgCount);
        result.put("neuroimmune", neuroCount);
        return Result.success(result);
    }

    @PostMapping("/online/kick")
    public Result<Void> kickUser(@RequestBody Map<String, Object> params) {
        Long userId = ((Number) params.get("userId")).longValue();
        String role = (String) params.get("role");
        String module = (String) params.get("module");
        boolean addToBlacklist = params.get("addToBlacklist") != null
            && Boolean.TRUE.equals(params.get("addToBlacklist"));
        int banHours = params.get("banHours") != null
            ? ((Number) params.get("banHours")).intValue()
            : blacklistService.getDefaultBanHours();

        // 踢下线
        tokenManagementService.kickUser(userId, role, module);

        // 记录审计日志
        auditService.recordKickOffline(userId, role, module, 1L);

        // 可选：加入黑名单
        if (addToBlacklist) {
            blacklistService.addToBlacklist(userId, role, module, "踢下线后自动封禁", banHours, 1L);
        }

        log.info("踢用户下线: userId={}, role={}, module={}, addToBlacklist={}", userId, role, module, addToBlacklist);
        return Result.success();
    }

    // ========== 审计日志 ==========

    @PostMapping("/audit/list")
    public Result<PageResult<SessionLog>> getAuditLogs(@RequestBody Map<String, Object> params) {
        int page = params.get("page") != null ? ((Number) params.get("page")).intValue() : 1;
        int size = params.get("pageSize") != null ? ((Number) params.get("pageSize")).intValue() : 20;
        String module = params.get("module") != null ? (String) params.get("module") : null;
        String role = params.get("role") != null ? (String) params.get("role") : null;
        String operationType = params.get("operationType") != null ? (String) params.get("operationType") : null;

        LocalDateTime startTime = null;
        LocalDateTime endTime = null;
        if (params.get("startTime") != null) {
            startTime = LocalDateTime.parse((String) params.get("startTime"));
        }
        if (params.get("endTime") != null) {
            endTime = LocalDateTime.parse((String) params.get("endTime"));
        }

        Page<SessionLog> pageObj = auditService.queryLogs(module, role, operationType, startTime, endTime, page, size);
        return Result.success(PageResult.of(pageObj.getRecords(), pageObj.getTotal(), page, size));
    }

    @GetMapping("/audit/stats")
    public Result<Map<String, Object>> getAuditStats() {
        long todayOps = auditService.countTodayOperations();
        long loginCount = auditService.countTodayLogins();
        long kickCount = auditService.countTodayKickOffline();
        Map<String, Object> result = new HashMap<>();
        result.put("todayOperations", todayOps);
        result.put("loginCount", loginCount);
        result.put("kickCount", kickCount);
        return Result.success(result);
    }

    // ========== 黑名单管理 ==========

    @PostMapping("/blacklist/list")
    public Result<List<BlackList>> getBlacklist(@RequestBody(required = false) Map<String, String> params) {
        String status = params != null ? params.get("status") : null;
        List<BlackList> list;
        if (status != null) {
            list = blacklistService.getAllBlacklist(status);
        } else {
            list = blacklistService.getActiveBlacklist();
        }
        return Result.success(list);
    }

    @GetMapping("/blacklist/count")
    public Result<Map<String, Object>> countBlacklist() {
        long activeCount = blacklistService.countActiveBlacklist();
        Map<String, Object> result = new HashMap<>();
        result.put("activeCount", activeCount);
        return Result.success(result);
    }

    @PostMapping("/blacklist/add")
    public Result<Void> addBlacklist(@RequestBody Map<String, Object> params) {
        Long userId = ((Number) params.get("userId")).longValue();
        String role = (String) params.get("role");
        String module = (String) params.get("module");
        String reason = (String) params.get("reason");
        int hours = params.get("hours") != null
            ? ((Number) params.get("hours")).intValue()
            : blacklistService.getDefaultBanHours();

        // 检查是否超过最大封禁时长
        int maxHours = blacklistService.getMaxBanHours();
        if (hours > maxHours) {
            hours = maxHours;
        }

        blacklistService.addToBlacklist(userId, role, module, reason, hours, 1L);
        return Result.success();
    }

    @PutMapping("/blacklist/release/{id}")
    public Result<Void> releaseBlacklist(@PathVariable Long id) {
        blacklistService.removeFromBlacklist(id);
        return Result.success();
    }

    // ========== 系统配置 ==========

    @GetMapping("/config/list")
    public Result<List<SystemConfig>> getConfigs() {
        List<SystemConfig> configs = systemConfigMapper.selectList(null);
        return Result.success(configs);
    }

    @PostMapping("/config/update")
    public Result<Void> updateConfig(@RequestBody Map<String, String> params) {
        String key = params.get("key");
        String value = params.get("value");

        if (key == null || value == null) {
            return Result.error(400, "参数不完整");
        }

        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SystemConfig> wrapper =
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.eq(SystemConfig::getConfigKey, key);
        SystemConfig config = systemConfigMapper.selectOne(wrapper);

        if (config != null) {
            config.setConfigValue(value);
            config.setUpdateTime(LocalDateTime.now());
            systemConfigMapper.updateById(config);
        } else {
            SystemConfig newConfig = new SystemConfig();
            newConfig.setConfigKey(key);
            newConfig.setConfigValue(value);
            systemConfigMapper.insert(newConfig);
        }

        return Result.success();
    }
}