package com.example.auth_service.application.port.out;

import com.example.auth_service.domain.model.User;

import java.time.Duration;

public interface TokenServicePort {
    String generateAccessToken(User user);

    String generateRefreshToken(User user);

    String getEmailFromToken(String token);

    boolean isRefreshTokenValid(String token, User user );

    boolean isAccessTokenValid(String token, User user );
    String extractBearerToken(String authorizationHeader);

    Duration getAccessTokenTtl();

    Duration getRefreshTokenTtl();

    Long getIdFromToken (String token);
}