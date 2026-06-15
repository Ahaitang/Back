package org.hospital.common.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 工具类
 * 负责 Token 的生成、解析、验证
 */
@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.secret:hospital-platform-jwt-secret-key-2024}")
    private String secret;

    @Value("${jwt.expiration:86400000}") // 默认 24 小时
    private Long expiration;

    private Key key;

    @PostConstruct
    public void init() {
        // 使用 HMAC-SHA256 对原始密钥进行派生，确保输出固定 32 字节且安全
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec("jwt-key-derive".getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] keyBytes = mac.doFinal(secret.getBytes(StandardCharsets.UTF_8));
            this.key = Keys.hmacShaKeyFor(keyBytes);
        } catch (Exception e) {
            throw new IllegalStateException("JWT 密钥初始化失败", e);
        }
    }

    /**
     * 生成 JWT Token
     */
    public String generateToken(UserInfo userInfo) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userInfo.getUserId());
        claims.put("username", userInfo.getUsername());
        claims.put("role", userInfo.getRole());
        claims.put("module", userInfo.getModule());

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userInfo.getUniqueId())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 解析 Token 获取用户信息
     */
    public UserInfo parseToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            UserInfo userInfo = new UserInfo();
            userInfo.setUserId(claims.get("userId", Long.class));
            userInfo.setUsername(claims.get("username", String.class));
            userInfo.setRole(claims.get("role", String.class));
            userInfo.setModule(claims.get("module", String.class));

            return userInfo;
        } catch (Exception e) {
            log.warn("解析 Token 失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 验证 Token 是否有效
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("Token 已过期: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("Token 格式不支持: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.warn("Token 格式错误: {}", e.getMessage());
        } catch (SignatureException e) {
            log.warn("Token 签名验证失败: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("Token 为空: {}", e.getMessage());
        }
        return false;
    }

    /**
     * 获取 Token 过期时间
     */
    public Date getExpirationDate(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.getExpiration();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 判断 Token 是否即将过期（小于 30 分钟）
     */
    public boolean isTokenExpiringSoon(String token) {
        Date expiration = getExpirationDate(token);
        if (expiration == null) {
            return true;
        }
        long remaining = expiration.getTime() - System.currentTimeMillis();
        return remaining < 30 * 60 * 1000; // 30 分钟
    }

    /**
     * 获取 Token 过期时间（秒）
     */
    public long getExpirationSeconds() {
        return expiration / 1000;
    }
}