package org.hospital.admin.controller;

import lombok.extern.slf4j.Slf4j;
import org.hospital.admin.config.SuperAdminConfig;
import org.hospital.admin.service.SuperAdminAuthService;
import org.hospital.common.model.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 超级管理员认证 Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/super-admin")
@CrossOrigin
public class AuthController {

    @Autowired
    private SuperAdminAuthService authService;

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
}