package org.hospital.common.config;

import org.hospital.common.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 统一 Spring Security 配置
 * JWT 认证 + 公开接口配置
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Value("${springdoc.api-docs.enabled:true}")
    private boolean swaggerEnabled;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // 基础公开接口
        List<String> publicPaths = new ArrayList<>(Arrays.asList(
            "/api/v1/qmg/doctor/login",
            "/api/v1/qmg/doctor/register",
            "/api/v1/qmg/doctor/getByUsername",
            "/api/v1/qmg/register/check-phone",
            "/api/v1/neuroimmune/login",
            "/api/v1/neuroimmune/register",
            "/api/v1/neuroimmune/register/check-phone",
            "/api/v1/neuroimmune/relation/doctors",
            "/api/v1/neuroimmune/import/*/template",
            "/api/v1/super-admin/login",
            "/actuator/health",
            "/actuator/info"
        ));

        // Swagger/Knife4j 仅在启用时开放
        if (swaggerEnabled) {
            publicPaths.addAll(Arrays.asList(
                "/doc.html",
                "/v3/api-docs/**",
                "/swagger-resources/**",
                "/webjars/**"
            ));
        }

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
                .requestMatchers(publicPaths.toArray(new String[0])).permitAll()
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