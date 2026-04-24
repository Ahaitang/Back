package org.hospital.common.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

/**
 * JWT 认证过滤器
 * 拦截请求验证 Token，设置用户身份
 */
@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private TokenStorage tokenStorage;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 1. 从请求头获取 Token
        String token = extractToken(request);

        if (!StringUtils.hasText(token)) {
            // 无 Token，继续执行（允许公开接口访问）
            filterChain.doFilter(request, response);
            return;
        }

        // 2. 解析并验证 Token
        if (!jwtUtil.validateToken(token)) {
            log.warn("Token 验证失败: {}", maskToken(token));
            filterChain.doFilter(request, response);
            return;
        }

        // 3. 获取用户信息
        UserInfo userInfo = jwtUtil.parseToken(token);
        if (userInfo == null) {
            log.warn("Token 解析失败，无法获取用户信息");
            filterChain.doFilter(request, response);
            return;
        }

        // 4. 验证 Token 是否在 Redis 中存在（防止伪造）
        if (!tokenStorage.validateTokenInRedis(userInfo, token)) {
            log.warn("Token 未在 Redis 中找到或已过期: userId={}, role={}", userInfo.getUserId(), userInfo.getRole());
            filterChain.doFilter(request, response);
            return;
        }

        // 5. 设置用户身份到 SecurityContext
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userInfo,
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + userInfo.getRole().toUpperCase()))
        );

        // 存储 UserInfo 供后续使用
        authentication.setDetails(userInfo);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        log.debug("认证成功: userId={}, username={}, role={}, module={}",
                userInfo.getUserId(), userInfo.getUsername(), userInfo.getRole(), userInfo.getModule());

        // 6. 继续执行请求
        filterChain.doFilter(request, response);
    }

    /**
     * 从请求头提取 Token
     */
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        // 也支持直接传 Token
        return request.getHeader("Token");
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