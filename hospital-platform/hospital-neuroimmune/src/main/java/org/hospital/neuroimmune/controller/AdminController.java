package org.hospital.neuroimmune.controller;

import org.hospital.neuroimmune.common.Result;
import org.hospital.neuroimmune.dto.LoginRequest;
import org.hospital.neuroimmune.entity.Admin;
import org.hospital.neuroimmune.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/neuroimmune/admin")
@CrossOrigin
public class AdminController {

    @Autowired
    private AdminService adminService;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody LoginRequest request) {
        Admin admin = adminService.login(request);
        if (admin != null) {
            Map<String, Object> data = new HashMap<>();
            data.put("token", "mock-token-" + admin.getId());
            data.put("admin", admin);
            return Result.success(data);
        }
        return Result.error(401, "用户名或密码错误");
    }

    @GetMapping("/info")
    public Result<Admin> info(@RequestParam Long id) {
        Admin admin = adminService.getById(id);
        return Result.success(admin);
    }
}