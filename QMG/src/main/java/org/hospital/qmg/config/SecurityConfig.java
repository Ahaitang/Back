package org.hospital.qmg.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

/**
 * Spring Security 配置类
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    /**
     * 配置HTTP安全策略
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
                // API接口不需要认证
                .antMatchers("/api/**").permitAll()
                // 静态资源不需要认证
                .antMatchers("/css/**", "/js/**", "/images/**", "/static/**").permitAll()
                // 其他请求需要认证
                .anyRequest().authenticated()
            .and()
            .formLogin()
                .loginPage("/login")
                .defaultSuccessUrl("/", true)
                .permitAll()
            .and()
            .logout()
                .logoutSuccessUrl("/login")
                .permitAll()
            .and()
            .csrf()
                // 禁用CSRF保护（API接口需要）
                .ignoringAntMatchers("/api/**");
    }

    /**
     * 配置用户详情服务（内存用户）
     */
    @Bean
    @Override
    public UserDetailsService userDetailsService() {
        UserDetails user = User.builder()
            .username("admin")
            .password(passwordEncoder().encode("123456"))
            .roles("USER", "ADMIN")
            .build();

        return new InMemoryUserDetailsManager(user);
    }

    /**
     * 密码编码器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
