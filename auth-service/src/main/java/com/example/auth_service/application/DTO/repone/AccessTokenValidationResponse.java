package com.example.auth_service.application.DTO.repone;

import java.util.Set;

import lombok.Builder;

@Builder
public record AccessTokenValidationResponse(
        Long userId,
        String email,
        String fullName,
        Set<String> role
) {
}
