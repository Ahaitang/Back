package org.hospital.common.config;

import org.hospital.common.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * 统一 Spring Security 配置
 * JWT 认证 + 公开接口配置
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 禁用 CSRF（JWT 不需要）
            .csrf(csrf -> csrf.disable())
            // 启用 CORS（由 CorsConfig 处理）
            .cors(cors -> {})
            // 无状态 Session（JWT 无状态）
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // 配置接口权限
            .authorizeHttpRequests(auth -> auth
                // 公开接口（无需认证）
                .requestMatchers(
                    "/api/v1/qmg/doctor/login",     // QMG 医生登录接口
                    "/api/v1/qmg/doctor/register",   // QMG 医生注册接口
                    "/api/v1/qmg/doctor/getByUsername", // QMG 获取医生信息（小程序用）
                    "/api/v1/qmg/register/check-phone", // QMG 检查手机号
                    "/api/v1/neuroimmune/login",   // Neuroimmune 登录接口
                    "/api/v1/neuroimmune/register",// Neuroimmune 注册接口
                    "/api/v1/neuroimmune/register/check-phone", // Neuroimmune 检查手机号
                    "/api/v1/neuroimmune/relation/doctors", // 医生列表（患者绑定用）
                    "/api/v1/neuroimmune/import/*/template", // 导入模板下载（公开）
                    "/api/v1/super-admin/login",   // 超级管理员登录
                    "/actuator/health",            // 健康检查
                    "/actuator/info",              // 应用信息
                    "/doc.html",                   // Knife4j 文档入口
                    "/v3/api-docs/**",             // API 文档 JSON
                    "/swagger-resources/**",       // Swagger 资源
                    "/webjars/**"                  // Web 资源
                ).permitAll()
                // 其他接口需要认证
                .anyRequest().authenticated())
            // 禁用表单登录
            .formLogin(form -> form.disable())
            // 禁用 HTTP Basic 认证
            .httpBasic(basic -> basic.disable())
            // 禁用登出
            .logout(logout -> logout.disable())
            // 添加 JWT 过滤器
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}