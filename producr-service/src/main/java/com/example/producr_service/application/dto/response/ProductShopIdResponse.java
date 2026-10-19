package com.example.producr_service.application.dto.response;

// Hệ thống dùng ID người tạo sản phẩm làm ID shop, chưa có định danh shop riêng.
public record ProductShopIdResponse(Long shopId) {}
