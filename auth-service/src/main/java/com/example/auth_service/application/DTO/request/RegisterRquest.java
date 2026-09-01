package com.example.auth_service.application.DTO.request;

import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterRquest {
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    // Email dùng cho cả đăng ký và đăng nhập
    private String email;
    @NotBlank(message = "Password is required")
    @Length(min = 6,message ="Password must be at least 6 characters long.")
    // Mật khẩu người dùng nhập vào
    private String password;
    @NotBlank(message = "fullName is required")
    @Length(max = 30,message = "The name must not exceed 30 characters.")
    // Họ tên chỉ bắt buộc khi đăng ký
    private String fullName;
}
