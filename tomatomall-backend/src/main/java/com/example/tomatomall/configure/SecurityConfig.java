package com.example.tomatomall.configure;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    /**
     * 配置安全过滤器链
     * 定义HTTP请求的安全策略
     * @param http HttpSecurity对象，用于配置安全策略
     * @return SecurityFilterChain 安全过滤器链
     * @throws Exception 配置异常
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 配置请求授权策略
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()  // 允许所有请求，无需身份认证
                )
                // 禁用CSRF防护，因为项目使用token进行身份验证
                .csrf(csrf -> csrf.disable());  // 禁用 CSRF 防护（如果需要）

        // 构建并返回安全过滤器链
        return http.build();
    }

    /**
     * 配置密码编码器
     * 用于对用户密码进行加密和验证
     * @return PasswordEncoder 密码编码器实例
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        // 返回 BCryptPasswordEncoder 实例，使用BCrypt算法进行密码加密
        return new BCryptPasswordEncoder();
    }
}
