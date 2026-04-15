package org.hospital.common.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Token 存储服务
 * 使用 Redis 存储活跃 Token，支持踢下线和过期管理
 */
@Slf4j
@Component
public class TokenStorage {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 存储 Token 到 Redis
     * Key: token:{module}:{role}:{userId}
     * Value: JWT Token
     */
    public void storeToken(UserInfo userInfo, String token) {
        String key = userInfo.getRedisKey();
        long expiration = jwtUtil.getExpirationSeconds();
        redisTemplate.opsForValue().set(key, token, expiration, TimeUnit.SECONDS);
        log.info("Token 已存储: {} -> {}", key, maskToken(token));
    }

    /**
     * 从 Redis 获取 Token
     */
    public String getToken(UserInfo userInfo) {
        String key = userInfo.getRedisKey();
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 验证 Token 是否在 Redis 中存在且匹配
     */
    public boolean validateTokenInRedis(UserInfo userInfo, String token) {
        String storedToken = getToken(userInfo);
        if (storedToken == null) {
            log.warn("Token 在 Redis 中不存在: {}", userInfo.getRedisKey());
            return false;
        }
        return storedToken.equals(token);
    }

    /**
     * 删除 Token（踢下线）
     */
    public void removeToken(UserInfo userInfo) {
        String key = userInfo.getRedisKey();
        redisTemplate.delete(key);
        log.info("Token 已删除（踢下线）: {}", key);
    }

    /**
     * 删除指定用户的所有 Token（按模块和角色）
     */
    public void removeAllTokensByUser(Long userId, String role, String module) {
        String pattern = String.format("token:%s:%s:%d", module, role, userId);
        redisTemplate.delete(pattern);
        log.info("用户所有 Token 已删除: {}", pattern);
    }

    /**
     * 刷新 Token 过期时间
     */
    public void refreshExpiration(UserInfo userInfo) {
        String key = userInfo.getRedisKey();
        long expiration = jwtUtil.getExpirationSeconds();
        redisTemplate.expire(key, expiration, TimeUnit.SECONDS);
    }

    /**
     * 检查 Token 是否存在
     */
    public boolean exists(UserInfo userInfo) {
        String key = userInfo.getRedisKey();
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /**
     * 获取 Token 剩余过期时间（秒）
     */
    public Long getRemainingExpiration(UserInfo userInfo) {
        String key = userInfo.getRedisKey();
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    /**
     * 隐藏 Token 中间部分（日志输出）
     */
    private String maskToken(String token) {
        if (token == null || token.length() < 20) {
            return "****";
        }
        return token.substring(0, 10) + "..." + token.substring(token.length() - 10);
    }
}