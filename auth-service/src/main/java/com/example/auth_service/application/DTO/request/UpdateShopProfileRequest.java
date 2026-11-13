package com.example.auth_service.application.DTO.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Thông tin shop được phép sửa; logo cập nhật qua API riêng.
public record UpdateShopProfileRequest(
        @NotBlank(message = "Tên shop không được để trống")
        @Size(max = 100, message = "Tên shop tối đa 100 ký tự")
        String shopName,
        @Size(max = 2000, message = "Mô tả shop tối đa 2000 ký tự")
        String shopDescription) {
}
