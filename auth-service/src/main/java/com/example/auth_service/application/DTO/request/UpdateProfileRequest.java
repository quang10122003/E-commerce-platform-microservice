package com.example.auth_service.application.DTO.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Thông tin cá nhân được phép sửa; avatar cập nhật qua API riêng.
public record UpdateProfileRequest(
        @NotBlank(message = "Họ và tên không được để trống")
        @Size(max = 30, message = "Họ và tên tối đa 30 ký tự")
        String fullName) {
}
