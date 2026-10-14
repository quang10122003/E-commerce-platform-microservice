package com.example.auth_service.application.DTO.repone;

// Location là địa chỉ shop nguyên văn; không trả email, số điện thoại hoặc thông tin xác thực.
public record PublicShopInfoResponse(Long shopId, String shopName, String location) {}
