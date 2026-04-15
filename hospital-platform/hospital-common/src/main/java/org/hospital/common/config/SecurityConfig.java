package org.hospital.common.config;

import org.hospital.common.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * 统一 Spring Security 配置
 * JWT 认证 + 公开接口配置
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            // 禁用 CSRF（JWT 不需要）
            .csrf().disable()
            // 启用 CORS（由 CorsConfig 处理）
            .cors()
            .and()
            // 无状态 Session（JWT 无状态）
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            // 配置接口权限
            .authorizeRequests()
                // 公开接口（无需认证）
                .antMatchers(
                    "/api/v1/**/login",           // 登录接口
                    "/api/v1/**/register",        // 注册接口
                    "/api/v1/neuroimmune/import/**", // 导入模板下载
                    "/api/v1/neuroimmune/file/**",   // 文件下载（部分公开）
                    "/actuator/**",                // 健康检查
                    "/swagger-ui.html",            // Swagger UI 入口页面
                    "/swagger-ui/**",              // Swagger UI 资源
                    "/v3/api-docs/**",             // API 文档
                    "/swagger-resources/**",       // Swagger 资源
                    "/webjars/**"                  // Web 资源
                ).permitAll()
                // 其他接口需要认证
                .anyRequest().authenticated()
            .and()
            // 禁用表单登录
            .formLogin().disable()
            // 禁用 HTTP Basic 认证
            .httpBasic().disable()
            // 禁用登出
            .logout().disable()
            // 添加 JWT 过滤器
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}