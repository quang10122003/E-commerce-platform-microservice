package com.example.producr_service.infrastructure.security;

import com.example.common.security.ApiAccessDeniedHandler;
import com.example.common.security.ApiAuthenticationEntryPoint;
import com.example.common.security.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    private static final String[] PUBLIC_ENDPOINTS = {
            // Phải cho phép /error để Spring giữ mã lỗi gốc, tránh biến lỗi xử lý thành lỗi xác thực.
            "/error"
    };

    private static final String[] PUBLIC_GET_ENDPOINTS = {
            "/api/products",
            "/api/products/recommendations/top-selling",
            "/api/products/recommendations/similar/{productId:-?[0-9]+}",
            "/api/products/{productId:-?[0-9]+}/shop/overview",
            "/api/products/shops/{shopId}/products",
            "/api/products/shops/{shopId}/top-products",
            "/api/products/{productId:-?[0-9]+}",
            "/api/products/{productId:-?[0-9]+}/price-range",
            "/api/products/{productId:-?[0-9]+}/variants/{variantId:-?[0-9]+}/sale",
            "/api/categories",
            "/api/brands",
            "/api/user/flash-sales/items",
            "/api/user/flash-sales/items/top-selling"
    };

    private static final String[] PUBLIC_POST_ENDPOINTS = {
            "/api/internal/variants/info",
            "/api/products/search-index/sync"
    };

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
            ApiAuthenticationEntryPoint authenticationEntryPoint,
            ApiAccessDeniedHandler accessDeniedHandler)
            throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Prometheus lấy số liệu định kỳ không có JWT nên chỉ mở riêng endpoint này; actuator khác vẫn yêu cầu ADMIN.
                        .requestMatchers(HttpMethod.GET, "/actuator/prometheus").permitAll()
                        .requestMatchers("/actuator/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                        .requestMatchers(HttpMethod.POST, PUBLIC_POST_ENDPOINTS).permitAll()
                        .requestMatchers(HttpMethod.GET, PUBLIC_GET_ENDPOINTS).permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(errors -> errors
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
