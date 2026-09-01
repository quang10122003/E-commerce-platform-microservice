package com.example.auth_service.application.DTO.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    // Email dùng cho cả đăng ký và đăng nhập
    private String email;
    
    @NotBlank(message = "Password is required")
    // Mật khẩu người dùng nhập vào
    private String password;

    
}
