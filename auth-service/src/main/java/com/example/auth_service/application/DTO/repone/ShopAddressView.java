package com.example.auth_service.application.DTO.repone;

// Projection giới hạn dữ liệu đọc ở ID, tên và địa chỉ shop.
public record ShopAddressView(Long shopId, String shopName, String shopAddress) {}
