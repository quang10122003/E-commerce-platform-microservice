package com.example.getway.application.service;

import com.example.common.error.AuthorizationError;
import com.example.common.exception.BusinessException;
import com.example.getway.application.model.TokenIdentity;
import com.example.getway.application.port.out.AccessControlCachePort;
import com.example.getway.application.port.out.TokenVerificationPort;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

// Điều phối xác thực token và kiểm tra trạng thái truy cập tại Gateway.
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AccessControlService {

    TokenVerificationPort tokenVerificationPort;
    AccessControlCachePort accessControlCachePort;

    // Xác thực token, kiểm tra user bị khóa và kiểm tra token bị thu hồi.
    public void authorize(String token) {
        TokenIdentity identity = tokenVerificationPort.verify(token);

        // Chặn user bị khóa trước khi kiểm tra token bị thu hồi.
        if (accessControlCachePort.isUserLocked(identity.userId())) {
            throw new BusinessException(AuthorizationError.USER_LOCKED);
        }

        // Chặn token đã bị logout hoặc thu hồi.
        if (accessControlCachePort.isTokenBlacklisted(token)) {
            throw new BusinessException(AuthorizationError.TOKEN_BLACKLISTED);
        }
    }
}
