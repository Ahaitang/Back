package org.hospital.neuroimmune.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Spring Security 配置
 * 禁用默认安全认证，使用自定义认证逻辑
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            // 禁用 CSRF
            .csrf().disable()
            // 禁用 CORS（由我们自己的 CorsFilter 处理）
            .cors().disable()
            // 允许所有请求
            .authorizeRequests()
                .antMatchers("/**").permitAll()
            .and()
            // 禁用表单登录
            .formLogin().disable()
            // 禁用 HTTP Basic 认证
            .httpBasic().disable()
            // 禁用登出
            .logout().disable();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}