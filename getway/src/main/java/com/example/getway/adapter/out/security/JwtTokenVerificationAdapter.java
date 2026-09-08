package com.example.getway.adapter.out.security;

import com.example.common.error.AuthorizationError;
import com.example.common.exception.BusinessException;
import com.example.common.security.JwtVerifier;
import com.example.getway.application.model.TokenIdentity;
import com.example.getway.application.port.out.TokenVerificationPort;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import org.springframework.stereotype.Component;

// Adapter xác thực chữ ký và các claim của JWT bằng public key.
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class JwtTokenVerificationAdapter implements TokenVerificationPort {

    JwtVerifier jwtVerifier;

    @Override
    public TokenIdentity verify(String token) {
        try {
            Claims claims = jwtVerifier.extractAllClaims(token);
            String tokenType = claims.get("tokenType", String.class);
            Object userId = claims.get("userId");

            // Chỉ access token mới được phép đi tới các API phía sau Gateway.
            if (!"access".equals(tokenType) || userId == null) {
                throw new BusinessException(AuthorizationError.BEARER_TOKEN_INVALID);
            }

            return new TokenIdentity(String.valueOf(userId));
        } catch (BusinessException ex) {
            throw ex;
        } catch (JwtException | IllegalArgumentException ex) {
            throw new BusinessException(AuthorizationError.BEARER_TOKEN_INVALID, ex);
        }
    }
}
