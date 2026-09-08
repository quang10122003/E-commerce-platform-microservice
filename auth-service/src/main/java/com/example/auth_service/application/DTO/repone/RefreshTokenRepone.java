package com.example.auth_service.application.DTO.repone;

import lombok.Builder;

@Builder
public record RefreshTokenRepone(
        String accessToken,
        String refreshToken
) {
}
