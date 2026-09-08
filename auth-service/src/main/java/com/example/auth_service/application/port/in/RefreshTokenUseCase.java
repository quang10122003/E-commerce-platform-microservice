package com.example.auth_service.application.port.in;

import com.example.auth_service.application.DTO.repone.RefreshTokenRepone;
import com.example.auth_service.application.DTO.request.RefreshTokenRequest;

public interface RefreshTokenUseCase {
    RefreshTokenRepone refreshToken(RefreshTokenRequest request);
}
