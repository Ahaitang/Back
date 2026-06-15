package org.hospital.admin.service;

import lombok.extern.slf4j.Slf4j;
import org.hospital.common.security.OnlineUser;
import org.hospital.common.security.TokenStorage;
import org.hospital.common.security.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Token 管理服务
 * 提供在线用户查询、踢下线等管理功能
 */
@Slf4j
@Service
public class TokenManagementService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private TokenStorage tokenStorage;

    /**
     * 获取所有在线用户
     * 返回格式：List<OnlineUser>
     */
    public List<OnlineUser> getOnlineUsers(String module) {
        String pattern = "token:" + module + ":*:*";
        Set<String> keys = redisTemplate.keys(pattern);

        List<OnlineUser> users = new ArrayList<>();
        if (keys == null || keys.isEmpty()) {
            return users;
        }

        for (String key : keys) {
            // 解析 key: token:{module}:{role}:{userId}
            String[] parts = key.split(":");
            if (parts.length >= 4) {
                try {
                    OnlineUser user = new OnlineUser();
                    user.setModule(parts[1]);
                    user.setRole(parts[2]);
                    user.setUserId(Long.parseLong(parts[3]));
                    user.setRedisKey(key);

                    // 获取剩余过期时间
                    Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
                    user.setTtlSeconds(ttl);

                    users.add(user);
                } catch (NumberFormatException e) {
                    log.warn("Redis key 格式异常，跳过: {}", key);
                }
            }
        }

        return users;
    }

    /**
     * 获取指定角色的在线用户
     */
    public List<OnlineUser> getOnlineUsersByRole(String module, String role) {
        String pattern = "token:" + module + ":" + role + ":*";
        Set<String> keys = redisTemplate.keys(pattern);

        List<OnlineUser> users = new ArrayList<>();
        if (keys == null || keys.isEmpty()) {
            return users;
        }

        for (String key : keys) {
            String[] parts = key.split(":");
            if (parts.length >= 4) {
                try {
                    OnlineUser user = new OnlineUser();
                    user.setModule(parts[1]);
                    user.setRole(parts[2]);
                    user.setUserId(Long.parseLong(parts[3]));
                    user.setRedisKey(key);

                    Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
                    user.setTtlSeconds(ttl);

                    users.add(user);
                } catch (NumberFormatException e) {
                    log.warn("Redis key 格式异常，跳过: {}", key);
                }
            }
        }

        return users;
    }

    /**
     * 踢指定用户下线
     */
    public boolean kickUser(Long userId, String role, String module) {
        UserInfo userInfo = new UserInfo(userId, null, role, module);
        String key = userInfo.getRedisKey();

        if (!tokenStorage.exists(userInfo)) {
            log.warn("用户不在线或 Token 已过期: {}", key);
            return false;
        }

        tokenStorage.removeToken(userInfo);
        log.info("踢用户下线成功: userId={}, role={}, module={}", userId, role, module);
        return true;
    }

    /**
     * 踢指定模块所有用户下线
     */
    public int kickAllUsersByModule(String module) {
        String pattern = "token:" + module + ":*:*";
        Set<String> keys = redisTemplate.keys(pattern);

        if (keys == null || keys.isEmpty()) {
            return 0;
        }

        int count = 0;
        for (String key : keys) {
            redisTemplate.delete(key);
            count++;
        }

        log.info("踢模块所有用户下线: module={}, count={}", module, count);
        return count;
    }

    /**
     * 踢指定角色所有用户下线
     */
    public int kickAllUsersByRole(String module, String role) {
        String pattern = "token:" + module + ":" + role + ":*";
        Set<String> keys = redisTemplate.keys(pattern);

        if (keys == null || keys.isEmpty()) {
            return 0;
        }

        int count = 0;
        for (String key : keys) {
            redisTemplate.delete(key);
            count++;
        }

        log.info("踢角色所有用户下线: module={}, role={}, count={}", module, role, count);
        return count;
    }

    /**
     * 统计在线用户数量
     */
    public long countOnlineUsers(String module) {
        String pattern = "token:" + module + ":*:*";
        Set<String> keys = redisTemplate.keys(pattern);
        return keys != null ? keys.size() : 0;
    }

    /**
     * 检查用户是否在线
     */
    public boolean isUserOnline(Long userId, String role, String module) {
        UserInfo userInfo = new UserInfo(userId, null, role, module);
        return tokenStorage.exists(userInfo);
    }
}