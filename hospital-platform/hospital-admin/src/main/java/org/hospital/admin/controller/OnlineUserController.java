package org.hospital.admin.controller;

import lombok.extern.slf4j.Slf4j;
import org.hospital.admin.model.KickUserRequest;
import org.hospital.admin.service.BlacklistService;
import org.hospital.admin.service.SessionAuditService;
import org.hospital.common.model.Result;
import org.hospital.admin.service.TokenManagementService;
import org.hospital.common.security.OnlineUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 在线用户管理 Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/super-admin/online")
@CrossOrigin
public class OnlineUserController {

    @Autowired
    private TokenManagementService tokenManagementService;

    @Autowired
    private SessionAuditService auditService;

    @Autowired
    private BlacklistService blacklistService;

    @Value("${admin.modules:neuroimmune}")
    private String modulesConfig;

    private List<String> getModules() {
        return Arrays.asList(modulesConfig.split(","));
    }

    @RequestMapping(value = "/list", method = {RequestMethod.GET, RequestMethod.POST})
    public Result<List<OnlineUser>> getOnlineUsers(
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String role) {
        List<OnlineUser> users;
        if (module != null && role != null) {
            users = tokenManagementService.getOnlineUsersByRole(module, role);
        } else if (module != null) {
            users = tokenManagementService.getOnlineUsers(module);
        } else {
            // 获取所有配置模块的在线用户
            users = new ArrayList<>();
            for (String m : getModules()) {
                users.addAll(tokenManagementService.getOnlineUsers(m.trim()));
            }
        }

        return Result.success(users);
    }

    @PostMapping("/count")
    public Result<Map<String, Object>> countOnline() {
        Map<String, Object> result = new HashMap<>();
        long total = 0;
        for (String m : getModules()) {
            long count = tokenManagementService.countOnlineUsers(m.trim());
            result.put(m.trim(), count);
            total += count;
        }
        result.put("total", total);
        return Result.success(result);
    }

    @PostMapping("/kick")
    public Result<Void> kickUser(@RequestBody @Validated KickUserRequest request) {
        Long userId = request.getUserId();
        String role = request.getRole();
        String module = request.getModule();
        boolean addToBlacklist = Boolean.TRUE.equals(request.getAddToBlacklist());
        int banHours = request.getBanHours() != null
            ? request.getBanHours()
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
}