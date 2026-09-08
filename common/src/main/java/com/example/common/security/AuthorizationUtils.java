package com.example.common.security;


import com.example.common.error.AuthorizationError;
import com.example.common.exception.BusinessException;

// Cung cấp hàm dùng chung để đọc Bearer token từ Authorization header.
public final class AuthorizationUtils {

    private static final String BEARER_PREFIX = "Bearer ";

    private AuthorizationUtils() {
        // Ngăn khởi tạo utility class.
    }

    // Kiểm tra Authorization header và trả về Bearer token đã được chuẩn hóa.
    public static String extractBearerToken(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            throw new BusinessException(AuthorizationError.AUTHORIZATION_HEADER_REQUIRED);
        }

        String headerValue = authorizationHeader.trim();
        if (!headerValue.regionMatches(true, 0, BEARER_PREFIX, 0, BEARER_PREFIX.length())) {
            throw new BusinessException(AuthorizationError.AUTHORIZATION_HEADER_INVALID);
        }

        String token = headerValue.substring(BEARER_PREFIX.length()).trim();
        if (token.isBlank()) {
            throw new BusinessException(AuthorizationError.BEARER_TOKEN_INVALID);
        }

        return token;
    }
}
