package org.hospital.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.hospital.admin.entity.SystemConfig;
import org.hospital.admin.mapper.SystemConfigMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 系统配置 Service
 */
@Service
public class SystemConfigService {

    @Autowired
    private SystemConfigMapper systemConfigMapper;

    public List<SystemConfig> getAllConfigs() {
        return systemConfigMapper.selectList(null);
    }

    public SystemConfig getByKey(String key) {
        LambdaQueryWrapper<SystemConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemConfig::getConfigKey, key);
        return systemConfigMapper.selectOne(wrapper);
    }

    public void updateConfig(String key, String value) {
        SystemConfig config = getByKey(key);
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
    }
}