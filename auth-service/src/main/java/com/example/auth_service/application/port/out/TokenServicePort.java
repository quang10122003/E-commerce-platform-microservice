package com.example.auth_service.application.port.out;

import com.example.auth_service.domain.model.User;

import java.time.Duration;

public interface TokenServicePort {
    String generateAccessToken(User user);

    String generateRefreshToken(User user);

    String getEmailFromToken(String token);

    boolean isRefreshTokenValid(String token, User user );

    boolean isAccessTokenValid(String token, User user );
    // lấy token từ authorizationHeader
    String extractBearerToken(String authorizationHeader);

    // lấy thời gian hết hạn AccessToken theo Duration
    Duration getAccessTokenTtl();

    // lấy thời gian hết hạn RefreshToken theo Duration
    Duration getRefreshTokenTtl();

    Long getIdFromToken (String token);
}