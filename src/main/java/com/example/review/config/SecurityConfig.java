package com.example.review.config;

import com.example.review.service.CustomUserDetailsService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.http.HttpMethod;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;

import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(
            CustomUserDetailsService userDetailsService
    ) {
        this.userDetailsService = userDetailsService;
    }

    // ==================================================
    // MÃ HÓA MẬT KHẨU
    // ==================================================

    @Bean
    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();
    }

    // ==================================================
    // XÁC THỰC TÀI KHOẢN TỪ DATABASE
    // ==================================================

    @Bean
    public AuthenticationProvider authenticationProvider(
            PasswordEncoder passwordEncoder
    ) {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider();

        provider.setUserDetailsService(
                userDetailsService
        );

        provider.setPasswordEncoder(
                passwordEncoder
        );

        return provider;
    }

    // ==================================================
    // CẤU HÌNH BẢO MẬT
    // ==================================================

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            AuthenticationProvider authenticationProvider
    ) throws Exception {

        http
                .authenticationProvider(
                        authenticationProvider
                )

                // API đánh giá công khai không cần CSRF
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers(
                                "/api/public/reviews/**"
                        )
                )

                .authorizeHttpRequests(auth -> auth

                        // ==========================================
                        // FILE TĨNH VÀ TRANG CÔNG KHAI
                        // ==========================================

                        .requestMatchers(
                                "/",
                                "/login",
                                "/review/**",
                                "/api/public/reviews/**",

                                "/css/**",
                                "/js/**",
                                "/images/**",

                                "/manifest.json",
                                "/service-worker.js",

                                "/favicon.ico",
                                "/error",
                                "/access-denied"
                        )
                        .permitAll()

                        // ==========================================
                        // QUẢN LÝ TÀI KHOẢN
                        // CHỈ MANAGER
                        // ==========================================

                        .requestMatchers(
                                "/manager/**"
                        )
                        .hasRole("MANAGER")

                        // ==========================================
                        // TRANG SỬA ĐÁNH GIÁ
                        // GET NHƯNG CHỈ MANAGER VÀ LEADER
                        // Phải đặt trước rule GET /admin/**
                        // ==========================================

                        .requestMatchers(
                                HttpMethod.GET,
                                "/admin/reviews/*/edit"
                        )
                        .hasAnyRole(
                                "MANAGER",
                                "LEADER"
                        )

                        // ==========================================
                        // TẤT CẢ URL QUẢN LÝ ĐÁNH GIÁ
                        // Chỉ Manager và Leader
                        // ==========================================

                        .requestMatchers(
                                "/admin/reviews/**"
                        )
                        .hasAnyRole(
                                "MANAGER",
                                "LEADER"
                        )

                        // ==========================================
                        // POST: THÊM, SỬA, XÓA
                        // Manager và Leader
                        // ==========================================

                        .requestMatchers(
                                HttpMethod.POST,
                                "/admin/**",
                                "/api/admin/**"
                        )
                        .hasAnyRole(
                                "MANAGER",
                                "LEADER"
                        )

                        // ==========================================
                        // PUT: CẬP NHẬT
                        // Manager và Leader
                        // ==========================================

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/admin/**",
                                "/api/admin/**"
                        )
                        .hasAnyRole(
                                "MANAGER",
                                "LEADER"
                        )

                        // ==========================================
                        // PATCH: CẬP NHẬT MỘT PHẦN
                        // Manager và Leader
                        // ==========================================

                        .requestMatchers(
                                HttpMethod.PATCH,
                                "/admin/**",
                                "/api/admin/**"
                        )
                        .hasAnyRole(
                                "MANAGER",
                                "LEADER"
                        )

                        // ==========================================
                        // DELETE: XÓA
                        // Manager và Leader
                        // ==========================================

                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/admin/**",
                                "/api/admin/**"
                        )
                        .hasAnyRole(
                                "MANAGER",
                                "LEADER"
                        )

                        // ==========================================
                        // GET: XEM DASHBOARD, BÁO CÁO
                        // Cả ba role được xem
                        // ==========================================

                        .requestMatchers(
                                HttpMethod.GET,
                                "/admin/**",
                                "/api/admin/**"
                        )
                        .hasAnyRole(
                                "MANAGER",
                                "LEADER",
                                "EMPLOYEE"
                        )

                        // Các URL còn lại phải đăng nhập
                        .anyRequest()
                        .authenticated()
                )

                // ==================================================
                // ĐĂNG NHẬP
                // ==================================================

                .formLogin(form -> form

                        .loginPage("/login")

                        .loginProcessingUrl("/login")

                        .defaultSuccessUrl(
                                "/admin/dashboard",
                                true
                        )

                        .failureUrl(
                                "/login?error"
                        )

                        .permitAll()
                )

                // ==================================================
                // ĐĂNG XUẤT
                // ==================================================

                .logout(logout -> logout

                        .logoutUrl("/logout")

                        .logoutSuccessUrl(
                                "/login?logout"
                        )

                        .invalidateHttpSession(true)

                        .deleteCookies(
                                "JSESSIONID"
                        )

                        .permitAll()
                )

                // ==================================================
                // KHÔNG ĐỦ QUYỀN
                // ==================================================

                .exceptionHandling(exception -> exception

                        .accessDeniedPage(
                                "/access-denied"
                        )
                );

        return http.build();
    }
}