package com.example.auth_service.adapter.out;

import org.springframework.stereotype.Component;

import com.example.auth_service.application.port.out.TokenServicePort;
import com.example.auth_service.domain.model.User;
import com.example.auth_service.infrastructure.sercurity.AuthUtil;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
@RequiredArgsConstructor
@Component
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class TokenServiceAdapter implements TokenServicePort {
    AuthUtil authUtil;

    @Override
    public String generateAccessToken(User user) {
        return authUtil.generateAccessToken(user);
        
    }

    @Override
    public String generateRefreshToken(User user) {
       return authUtil.generateRefreshToken(user);
    }
    
}
