package com.example.auth_service.application.DTO.repone;

import com.example.auth_service.domain.model.RoleEnum;

import java.util.Set;

import lombok.Builder;

@Builder
public record AccessTokenValidationResponse(
        Long userId,
        String email,
        String fullName,
        Set<RoleEnum> role
) {
}
