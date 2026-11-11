package com.example.auth_service.application.DTO.repone;

import com.example.auth_service.domain.model.RoleEnum;

import java.time.Instant;
import java.util.List;

public record UserListResponse(Long id, String email, String fullName, boolean locked,
                               Instant createdAt, List<RoleEnum> roles,
                               String shopName, String shopLogoUrl, String shopDescription,
                               String shopPhone, String shopAddress, Instant shopCreatedAt,
                               boolean shopLocked, Instant deletedAt) {
}
