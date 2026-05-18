package org.hospital.admin.controller;

import lombok.extern.slf4j.Slf4j;
import org.hospital.admin.entity.SystemConfig;
import org.hospital.admin.service.SystemConfigService;
import org.hospital.common.model.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 系统配置 Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/super-admin/config")
@CrossOrigin
public class SystemConfigController {

    @Autowired
    private SystemConfigService systemConfigService;

    @RequestMapping(value = "/list", method = {RequestMethod.GET, RequestMethod.POST})
    public Result<List<SystemConfig>> getConfigs() {
        List<SystemConfig> configs = systemConfigService.getAllConfigs();
        return Result.success(configs);
    }

    @PostMapping("/update")
    public Result<Void> updateConfig(@RequestBody Map<String, String> params) {
        String key = params.get("key");
        String value = params.get("value");

        if (key == null || value == null) {
            return Result.error(400, "参数不完整");
        }

        systemConfigService.updateConfig(key, value);
        return Result.success();
    }
}