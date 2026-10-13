package com.example.notification.application.dto.response;

// Thông tin người đăng nhập phục vụ các use case của notification.
public record CurrentUserInfoResponse(
        Long userId,
        String email,
        String fullName,
        String shopName,
        String shopPhone,
        String shopAddress,
        boolean isShopLock
) {}
