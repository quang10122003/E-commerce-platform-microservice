package com.example.auth_service.application.DTO.repone;

// Địa chỉ shop chưa tách cấu trúc; response công khai không được chứa dữ liệu liên hệ.
public record PublicShopInfoResponse(Long shopId, String shopName, String location) {}
