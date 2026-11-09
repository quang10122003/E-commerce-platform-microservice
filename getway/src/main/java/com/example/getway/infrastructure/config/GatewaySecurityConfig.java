package com.example.getway.infrastructure.config;

import com.example.common.security.JwtVerifier;
import com.example.getway.adapter.in.security.JwtAuthenticationWebFilter;
import com.example.getway.application.port.out.cache.CachePort;
import com.example.getway.application.port.in.AuthorizeAccessUseCase;
import com.example.getway.application.port.out.TokenVerificationPort;
import com.example.getway.application.service.AccessControlService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.HttpStatusServerEntryPoint;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;

@Configuration
public class GatewaySecurityConfig {

    @Bean
    JwtVerifier jwtVerifier(@Value("${app.jwt.public-key}") String publicKey) {
        return new JwtVerifier(publicKey);
    }

    @Bean
    AuthorizeAccessUseCase accessControlService(
            TokenVerificationPort tokenVerificationPort,
            CachePort cachePort) {
        return new AccessControlService(tokenVerificationPort, cachePort);
    }

    // Chain chỉ áp dụng cho actuator để không ảnh hưởng các route; route vẫn do GatewayAccessControlFilter kiểm tra token.
    @Bean
    SecurityWebFilterChain actuatorSecurityWebFilterChain(ServerHttpSecurity http, JwtVerifier jwtVerifier) {
        return http
                .securityMatcher(ServerWebExchangeMatchers.pathMatchers("/actuator/**"))
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .logout(ServerHttpSecurity.LogoutSpec::disable)
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
                .authorizeExchange(exchange -> exchange.anyExchange().hasAuthority("ROLE_ADMIN"))
                .exceptionHandling(errors -> errors
                        .authenticationEntryPoint(new HttpStatusServerEntryPoint(HttpStatus.UNAUTHORIZED)))
                .addFilterAt(new JwtAuthenticationWebFilter(jwtVerifier), SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }
}
