package org.hospital.common.controller;

import org.hospital.common.audit.AuditLog;
import org.hospital.common.audit.OperationType;
import org.hospital.common.model.Result;
import org.hospital.common.security.OnlineUser;
import org.hospital.common.security.TokenManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Token 管理接口
 * 查看在线用户、踢下线等管理功能
 */
@RestController
@RequestMapping("/api/v1/admin/session")
@CrossOrigin
public class TokenManagementController {

    @Autowired
    private TokenManagementService tokenManagementService;

    /**
     * 获取在线用户列表
     * 参数：module (qmg/neuroimmune), role (admin/doctor/patient/manager)
     */
    @AuditLog(operation = OperationType.QUERY, module = "会话管理", description = "查询在线用户列表")
    @PostMapping("/online")
    public Result<List<OnlineUser>> getOnlineUsers(@RequestBody(required = false) Map<String, String> params) {
        String module = params != null ? params.get("module") : null;
        String role = params != null ? params.get("role") : null;

        List<OnlineUser> users;
        if (role != null && !role.isEmpty()) {
            users = tokenManagementService.getOnlineUsersByRole(module, role);
        } else {
            users = tokenManagementService.getOnlineUsers(module);
        }

        return Result.success(users);
    }

    /**
     * 统计在线用户数量
     */
    @PostMapping("/count")
    public Result<Map<String, Object>> countOnlineUsers(@RequestBody(required = false) Map<String, String> params) {
        String module = params != null ? params.get("module") : "all";

        long count;
        if ("all".equals(module)) {
            count = tokenManagementService.countOnlineUsers("qmg")
                   + tokenManagementService.countOnlineUsers("neuroimmune");
        } else {
            count = tokenManagementService.countOnlineUsers(module);
        }

        Map<String, Object> data = new java.util.HashMap<>();
        data.put("module", module);
        data.put("count", count);

        return Result.success(data);
    }

    /**
     * 踢指定用户下线
     * 参数：userId, role, module
     */
    @AuditLog(operation = OperationType.KICK_OFFLINE, module = "会话管理", description = "踢用户下线")
    @DeleteMapping("/kick")
    public Result<Void> kickUser(@RequestBody Map<String, Object> params) {
        Long userId = params.get("userId") != null ? ((Number) params.get("userId")).longValue() : null;
        String role = (String) params.get("role");
        String module = (String) params.get("module");

        if (userId == null || role == null || module == null) {
            return Result.error(400, "参数不完整：需要 userId, role, module");
        }

        boolean success = tokenManagementService.kickUser(userId, role, module);
        if (success) {
            return Result.success();
        } else {
            return Result.error(404, "用户不在线或已过期");
        }
    }

    /**
     * 踢模块所有用户下线
     */
    @AuditLog(operation = OperationType.KICK_OFFLINE, module = "会话管理", description = "踢模块所有用户下线")
    @DeleteMapping("/kick-all/module")
    public Result<Map<String, Object>> kickAllByModule(@RequestBody Map<String, String> params) {
        String module = params.get("module");
        if (module == null || module.isEmpty()) {
            return Result.error(400, "需要指定 module 参数");
        }

        int count = tokenManagementService.kickAllUsersByModule(module);

        Map<String, Object> data = new java.util.HashMap<>();
        data.put("module", module);
        data.put("kickedCount", count);

        return Result.success(data);
    }

    /**
     * 踢角色所有用户下线
     */
    @AuditLog(operation = OperationType.KICK_OFFLINE, module = "会话管理", description = "踢角色所有用户下线")
    @DeleteMapping("/kick-all/role")
    public Result<Map<String, Object>> kickAllByRole(@RequestBody Map<String, String> params) {
        String module = params.get("module");
        String role = params.get("role");

        if (module == null || role == null) {
            return Result.error(400, "需要指定 module 和 role 参数");
        }

        int count = tokenManagementService.kickAllUsersByRole(module, role);

        Map<String, Object> data = new java.util.HashMap<>();
        data.put("module", module);
        data.put("role", role);
        data.put("kickedCount", count);

        return Result.success(data);
    }

    /**
     * 检查用户是否在线
     */
    @PostMapping("/check")
    public Result<Map<String, Object>> checkOnline(@RequestBody Map<String, Object> params) {
        Long userId = params.get("userId") != null ? ((Number) params.get("userId")).longValue() : null;
        String role = (String) params.get("role");
        String module = (String) params.get("module");

        if (userId == null || role == null || module == null) {
            return Result.error(400, "参数不完整");
        }

        boolean online = tokenManagementService.isUserOnline(userId, role, module);

        Map<String, Object> data = new java.util.HashMap<>();
        data.put("userId", userId);
        data.put("role", role);
        data.put("module", module);
        data.put("online", online);

        return Result.success(data);
    }
}