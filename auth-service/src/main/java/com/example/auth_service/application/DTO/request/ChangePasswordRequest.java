package com.example.auth_service.application.DTO.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// newPassword giới hạn 72 ký tự vì BCrypt chỉ dùng 72 byte đầu.
public record ChangePasswordRequest(
        @NotBlank(message = "Vui lòng nhập mật khẩu hiện tại")
        String currentPassword,
        @NotBlank(message = "Vui lòng nhập mật khẩu mới")
        @Size(min = 6, max = 72, message = "Mật khẩu mới từ 6 đến 72 ký tự")
        String newPassword,
        @NotBlank(message = "Vui lòng nhập lại mật khẩu mới")
        String confirmPassword) {
}
