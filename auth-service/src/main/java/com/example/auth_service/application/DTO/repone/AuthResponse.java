package com.example.auth_service.application.DTO.repone;

import com.example.auth_service.domain.model.RoleEnum;

import java.util.Set;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {

    private String accessToken;

    private String refreshToken;
    Set<RoleEnum> role;
    private Long userId;
    private String email;
    private String fullName;
    private String avatarUrl;
}
