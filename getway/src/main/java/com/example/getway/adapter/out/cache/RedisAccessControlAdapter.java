package com.example.getway.adapter.out.cache;

import com.example.common.security.AccessControlCacheKeys;
import com.example.getway.application.port.out.AccessControlCachePort;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

// Adapter đọc trạng thái blacklist token và khóa user từ Redis.
@Component
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RedisAccessControlAdapter implements AccessControlCachePort {

    StringRedisTemplate stringRedisTemplate;

    @Override
    public boolean isTokenBlacklisted(String token) {
        try {
            return Boolean.TRUE.equals(stringRedisTemplate.hasKey(
                    AccessControlCacheKeys.blacklistJwt(token)));
        } catch (DataAccessException ex) {
            throw accessControlUnavailable(ex);
        }
    }

    @Override
    public boolean isUserLocked(String userId) {
        try {
            return Boolean.TRUE.equals(stringRedisTemplate.hasKey(
                    AccessControlCacheKeys.userLocked(userId)));
        } catch (DataAccessException ex) {
            throw accessControlUnavailable(ex);
        }
    }

    // Chuyển lỗi Redis thành lỗi nghiệp vụ để Gateway trả response nhất quán.
    private RuntimeException accessControlUnavailable(DataAccessException ex) {
        log.error("Không thể kiểm tra trạng thái truy cập trên Redis", ex);
        return new com.example.common.exception.BusinessException(
                com.example.common.error.AuthorizationError.ACCESS_CONTROL_UNAVAILABLE,
                ex);
    }
}
