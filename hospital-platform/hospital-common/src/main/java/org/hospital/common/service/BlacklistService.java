package org.hospital.common.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.hospital.common.entity.BlackList;
import org.hospital.common.mapper.BlackListMapper;
import org.hospital.common.mapper.SystemConfigMapper;
import org.hospital.common.entity.SystemConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 黑名单服务
 */
@Slf4j
@Service
public class BlacklistService {

    @Autowired
    private BlackListMapper blackListMapper;

    @Autowired
    private SystemConfigMapper systemConfigMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 添加到黑名单
     */
    public void addToBlacklist(Long userId, String role, String module, String reason, int hours, Long operatorId) {
        BlackList bl = new BlackList();
        bl.setUserId(userId);
        bl.setRole(role);
        bl.setModule(module);
        bl.setReason(reason);
        bl.setBanTime(LocalDateTime.now());
        bl.setExpireTime(LocalDateTime.now().plusHours(hours));
        bl.setStatus("active");
        bl.setOperatorId(operatorId);
        blackListMapper.insert(bl);

        // 同步到 Redis
        String key = String.format("blacklist:%s:%s:%d", module, role, userId);
        redisTemplate.opsForValue().set(key, bl.getExpireTime().toString(), hours * 3600L, TimeUnit.SECONDS);
        log.info("用户加入黑名单: userId={}, role={}, module={}, hours={}", userId, role, module, hours);
    }

    /**
     * 检查是否在黑名单中
     */
    public boolean isInBlacklist(Long userId, String role, String module) {
        String key = String.format("blacklist:%s:%s:%d", module, role, userId);
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /**
     * 从黑名单移除
     */
    public void removeFromBlacklist(Long id) {
        BlackList bl = blackListMapper.selectById(id);
        if (bl != null) {
            bl.setStatus("released");
            bl.setUpdateTime(LocalDateTime.now());
            blackListMapper.updateById(bl);

            String key = String.format("blacklist:%s:%s:%d", bl.getModule(), bl.getRole(), bl.getUserId());
            redisTemplate.delete(key);
            log.info("用户从黑名单移除: userId={}, role={}, module={}", bl.getUserId(), bl.getRole(), bl.getModule());
        }
    }

    /**
     * 获取活跃黑名单列表
     */
    public List<BlackList> getActiveBlacklist() {
        LambdaQueryWrapper<BlackList> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BlackList::getStatus, "active")
               .orderByDesc(BlackList::getBanTime);
        return blackListMapper.selectList(wrapper);
    }

    /**
     * 获取所有黑名单（包含已释放）
     */
    public List<BlackList> getAllBlacklist(String status) {
        LambdaQueryWrapper<BlackList> wrapper = new LambdaQueryWrapper<>();
        if (status != null && !status.isEmpty()) {
            wrapper.eq(BlackList::getStatus, status);
        }
        wrapper.orderByDesc(BlackList::getBanTime);
        return blackListMapper.selectList(wrapper);
    }

    /**
     * 统计活跃黑名单数量
     */
    public long countActiveBlacklist() {
        LambdaQueryWrapper<BlackList> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BlackList::getStatus, "active");
        return blackListMapper.selectCount(wrapper);
    }

    /**
     * 获取默认封禁时长（小时）
     */
    public int getDefaultBanHours() {
        SystemConfig config = systemConfigMapper.selectOne(
            new LambdaQueryWrapper<SystemConfig>().eq(SystemConfig::getConfigKey, "default_ban_hours")
        );
        if (config != null) {
            return Integer.parseInt(config.getConfigValue());
        }
        return 24;
    }

    /**
     * 获取最大封禁时长（小时）
     */
    public int getMaxBanHours() {
        SystemConfig config = systemConfigMapper.selectOne(
            new LambdaQueryWrapper<SystemConfig>().eq(SystemConfig::getConfigKey, "max_ban_hours")
        );
        if (config != null) {
            return Integer.parseInt(config.getConfigValue());
        }
        return 72;
    }
}