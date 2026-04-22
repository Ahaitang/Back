package org.hospital.neuroimmune.controller;

import org.hospital.common.model.Result;
import org.hospital.common.model.LoginRequest;
import org.hospital.neuroimmune.entity.Admin;
import org.hospital.neuroimmune.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/neuroimmune/admin")
@CrossOrigin
public class AdminController {

    @Autowired
    private AdminService adminService;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody LoginRequest request) {
        Admin admin = adminService.login(request);
        if (admin != null) {
            Map<String, Object> data = new HashMap<>();
            // TODO: 使用真实JWT token生成，当前为临时方案
            // 实际应调用JWT服务生成token: jwtService.generateToken(admin)
            data.put("token", generateSecureToken(admin.getId()));
            data.put("admin", admin);
            data.put("role", "admin");  // 角色由后端确定
            return Result.success(data);
        }
        return Result.error(401, "用户名或密码错误");
    }

    // 临时token生成方法（应替换为真实JWT）
    private String generateSecureToken(Long userId) {
        // 生产环境必须使用JWT库生成真实token
        // 此方法仅用于过渡，不建议生产使用
        return java.util.UUID.randomUUID().toString() + "-" + userId + "-" + System.currentTimeMillis();
    }

    // 注意: /info接口已移除，应通过token验证获取用户信息
    // 如需查询用户信息，应在其他需要认证的接口中处理
}