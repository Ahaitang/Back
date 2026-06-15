package org.hospital.admin.controller;

import lombok.extern.slf4j.Slf4j;
import org.hospital.admin.entity.BlackList;
import org.hospital.admin.model.BlacklistAddRequest;
import org.hospital.admin.service.BlacklistService;
import org.hospital.common.model.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 黑名单管理 Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/super-admin/blacklist")
@CrossOrigin
public class BlacklistController {

    @Autowired
    private BlacklistService blacklistService;

    @PostMapping("/list")
    public Result<List<BlackList>> getBlacklist(@RequestBody(required = false) Map<String, String> params) {
        List<BlackList> list;
        String status = params != null ? params.get("status") : null;
        if (status != null) {
            list = blacklistService.getAllBlacklist(status);
        } else {
            list = blacklistService.getActiveBlacklist();
        }
        return Result.success(list);
    }

    @RequestMapping(value = "/count", method = {RequestMethod.GET, RequestMethod.POST})
    public Result<Map<String, Object>> countBlacklist() {
        long activeCount = blacklistService.countActiveBlacklist();
        Map<String, Object> result = new HashMap<>();
        result.put("activeCount", activeCount);
        return Result.success(result);
    }

    @PostMapping("/add")
    public Result<Void> addBlacklist(@RequestBody @Validated BlacklistAddRequest request) {
        Long userId = request.getUserId();
        String role = request.getRole();
        String module = request.getModule();
        String reason = request.getReason();
        int hours = request.getHours() != null
            ? request.getHours()
            : blacklistService.getDefaultBanHours();

        // 检查是否超过最大封禁时长
        int maxHours = blacklistService.getMaxBanHours();
        if (hours > maxHours) {
            hours = maxHours;
        }

        blacklistService.addToBlacklist(userId, role, module, reason, hours, 1L);
        return Result.success();
    }

    @PutMapping("/release/{id}")
    public Result<Void> releaseBlacklist(@PathVariable Long id) {
        blacklistService.removeFromBlacklist(id);
        return Result.success();
    }
}