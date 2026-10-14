package com.example.producr_service.adapter.client.dto;

// Location từ Auth là địa chỉ shop đầy đủ, chưa tách tỉnh/thành.
public record PublicShopInfoResponse(Long shopId, String shopName, String location) {}
