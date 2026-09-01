package com.example.auth_service.application.port.out;

import com.example.auth_service.domain.model.User;

public interface TokenServicePort {
    String generateAccessToken(User user);

    String generateRefreshToken(User user);
}