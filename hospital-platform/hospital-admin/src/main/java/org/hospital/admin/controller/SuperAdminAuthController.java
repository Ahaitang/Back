package org.hospital.admin.controller;

import org.hospital.common.model.Result;
import org.hospital.admin.service.SuperAdminAuthService;
import org.hospital.admin.config.SuperAdminConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;
import java.util.Map;
import java.util.HashMap;

@Slf4j
@RestController
@RequestMapping("/api/v1/super-admin")
@CrossOrigin
public class SuperAdminAuthController {

    @Autowired
    private SuperAdminAuthService authService;

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