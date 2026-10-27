package com.example.getway.application.service;

import com.example.common.error.AuthorizationError;
import com.example.common.exception.BusinessException;
import com.example.common.exception.CacheAccessException;
import com.example.common.security.AccessControlCacheKeys;
import com.example.getway.application.model.TokenIdentity;
import com.example.getway.application.port.in.AuthorizeAccessUseCase;
import com.example.getway.application.port.out.cache.CachePort;
import com.example.getway.application.port.out.TokenVerificationPort;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AccessControlService implements AuthorizeAccessUseCase {

    TokenVerificationPort tokenVerificationPort;
    CachePort cachePort;

    @Override
    public void authorize(String token) {
        TokenIdentity identity = tokenVerificationPort.verify(token);

        try {
            if (cachePort.exists(AccessControlCacheKeys.userLocked(identity.userId()))) {
                throw new BusinessException(AuthorizationError.USER_LOCKED);
            }
            if (cachePort.exists(AccessControlCacheKeys.blacklistJwt(token))) {
                throw new BusinessException(AuthorizationError.TOKEN_BLACKLISTED);
            }
        } catch (CacheAccessException exception) {
            // Gateway từ chối truy cập khi không kiểm tra được Redis, thay vì coi token là hợp lệ.
            log.error("Không thể kiểm tra trạng thái truy cập trên cache");
            throw new BusinessException(AuthorizationError.ACCESS_CONTROL_UNAVAILABLE, exception);
        }
    }
}
