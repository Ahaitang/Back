package org.hospital.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.hospital.admin.entity.SystemConfig;
import org.hospital.admin.mapper.SystemConfigMapper;
import org.hospital.common.model.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
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
    private SystemConfigMapper systemConfigMapper;

    @GetMapping("/list")
    public Result<List<SystemConfig>> getConfigs() {
        List<SystemConfig> configs = systemConfigMapper.selectList(null);
        return Result.success(configs);
    }

    @PostMapping("/update")
    public Result<Void> updateConfig(@RequestBody Map<String, String> params) {
        String key = params.get("key");
        String value = params.get("value");

        if (key == null || value == null) {
            return Result.error(400, "参数不完整");
        }

        LambdaQueryWrapper<SystemConfig> wrapper =
            new LambdaQueryWrapper<>();
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