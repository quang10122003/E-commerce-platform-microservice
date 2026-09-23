package com.example.auth_service.application.mapper;

import com.example.auth_service.application.DTO.repone.UserInternaInfoRespone;
import com.example.auth_service.domain.model.User;

public final class UserInternalInfoResponseMapper {

    private UserInternalInfoResponseMapper() {
    }

    // Chuyển thông tin User domain sang response dùng cho API nội bộ.
    public static UserInternaInfoRespone toResponse(User user) {
        if (user == null) {
            return null;
        }

        return UserInternaInfoRespone.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .shopName(user.getShopName())
                .shopPhone(user.getShopPhone())
                .shopAddress(user.getShopAddress())
                .isShopLock(user.isShopLock())
                .build();
    }
}
