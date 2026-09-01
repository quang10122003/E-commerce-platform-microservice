package com.example.auth_service.application.DTO.repone;

import java.util.Set;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {

    // Access token trả về sau khi xác thực thành công
    private String accessToken;

    // Refresh token để làm mới phiên đăng nhập
    private String refreshToken;
    Set<String> role;
    // Thông tin cơ bản của tài khoản
    private Long userId;
    private String email;
    private String fullName;
}
