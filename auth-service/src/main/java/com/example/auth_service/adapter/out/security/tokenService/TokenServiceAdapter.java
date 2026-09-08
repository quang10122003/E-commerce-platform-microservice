package com.example.auth_service.adapter.out.security.tokenService;

import com.example.auth_service.adapter.mapper.UserMapper;
import com.example.common.security.AuthorizationUtils;
import org.springframework.stereotype.Component;

import com.example.auth_service.application.port.out.TokenServicePort;
import com.example.auth_service.domain.model.User;
import com.example.auth_service.infrastructure.sercurity.AuthUtil;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.Duration;

@RequiredArgsConstructor
@Component
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class TokenServiceAdapter implements TokenServicePort {
    AuthUtil authUtil;
    UserMapper userMapper;

    @Override
    public String generateAccessToken(User user) {
        return authUtil.generateAccessToken(user);
        
    }

    @Override
    public String generateRefreshToken(User user) {
       return authUtil.generateRefreshToken(user);
    }

    @Override
    public String getEmailFromToken(String token) {
        return authUtil.extractEmail(token);
    }

    @Override
    public boolean isRefreshTokenValid(String token, User user) {
        return authUtil.isRefreshTokenValid(token,userMapper.toEntity(user));
    }

    @Override
    public boolean isAccessTokenValid(String token, User user) {
        return authUtil.isAccessTokenValid(token,userMapper.toEntity(user));
    }

    @Override
    public String extractBearerToken(String authorizationHeader) {
        return AuthorizationUtils.extractBearerToken(authorizationHeader);
    }

    @Override
    public Duration getAccessTokenTtl() {
        return Duration.ofMillis(authUtil.getAccessTokenExpirationMillis());
    }

    @Override
    public Duration getRefreshTokenTtl() {
        return Duration.ofMillis(authUtil.getRefreshTokenExpirationMillis());
    }

}
