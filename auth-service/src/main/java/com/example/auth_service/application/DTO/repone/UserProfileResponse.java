package com.example.auth_service.application.DTO.repone;

import com.example.auth_service.domain.model.AuthProvider;
import com.example.auth_service.domain.model.RoleEnum;

import java.time.OffsetDateTime;
import java.util.Set;

// Hồ sơ của chính tài khoản đang đăng nhập; createdAt kèm offset của BE.
public record UserProfileResponse(String email, String fullName, String avatarUrl,
                                  Set<RoleEnum> roles, OffsetDateTime createdAt,
                                  AuthProvider provider) {
}
