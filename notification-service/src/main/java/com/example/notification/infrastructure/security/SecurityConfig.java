package com.example.notification.infrastructure.security;

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
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private static final String[] PUBLIC_ENDPOINTS = {
            "/error",
            // Handshake chưa mang STOMP header; vé bắt buộc tại CONNECT, API cấp vé vẫn dùng JWT.
            "/ws-notifications",
            "/ws-notifications/**"
    };

    private static final String[] PUBLIC_GET_ENDPOINTS = {
            "/api/banners/active"
    };

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthFilter jwtAuthFilter,
                                          ApiAuthenticationEntryPoint authenticationEntryPoint,
                                          ApiAccessDeniedHandler accessDeniedHandler) throws Exception {
        // Giữ xác thực trong cùng request để lượt ASYNC không mất SecurityContext của JWT.
        http.csrf(csrf -> csrf.disable())
                .securityContext(context -> context.requireExplicitSave(false)
                        .securityContextRepository(new RequestAttributeSecurityContextRepository()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                        .requestMatchers(HttpMethod.GET, PUBLIC_GET_ENDPOINTS).permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(errors -> errors
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
