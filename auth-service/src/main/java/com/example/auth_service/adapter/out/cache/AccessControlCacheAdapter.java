package com.example.auth_service.adapter.out.cache;

import com.example.auth_service.adapter.out.cache.key.AuthCacheKeys;
import com.example.auth_service.application.port.out.AccessControlCachePort;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Component
@Slf4j
public class AccessControlCacheAdapter implements AccessControlCachePort {

    StringRedisTemplate stringRedisTemplate;

    @Override
    public void blacklistToken(String jwt, Duration ttl) {
        try {
            stringRedisTemplate.opsForValue().set(
                    AuthCacheKeys.blacklistJwt(jwt),
                    "1",
                ttl
            );
        } catch (DataAccessException ex) {
            log.error("Failed to blacklist JWT in Redis", ex);
        }
    }

    @Override
    public boolean isTokenBlacklisted(String jwt) {
        try {
            return Boolean.TRUE.equals(
                    stringRedisTemplate.hasKey(
                            AuthCacheKeys.blacklistJwt(jwt)
                    )
            );
        } catch (DataAccessException ex) {
            log.error("Failed to check JWT blacklist in Redis", ex);

            // Redis không kiểm tra được.
            return false;
        }
    }

    @Override
    public void lockUser(String userId) {
        try {
            stringRedisTemplate.opsForValue().set(
                    AuthCacheKeys.userLocked(userId),
                    "1"
            );
        } catch (DataAccessException ex) {
            log.error("Failed to lock user [{}] in Redis", userId, ex);
        }
    }

    @Override
    public boolean isUserLocked(String userId) {
        try {
            return Boolean.TRUE.equals(
                    stringRedisTemplate.hasKey(
                            AuthCacheKeys.userLocked(userId)
                    )
            );
        } catch (DataAccessException ex) {
            log.error("Failed to check user lock [{}] in Redis", userId, ex);

            // Redis không kiểm tra được.
            return false;
        }
    }

    @Override
    public void unlockUser(String userId) {
        try {
            stringRedisTemplate.delete(
                    AuthCacheKeys.userLocked(userId)
            );
        } catch (DataAccessException ex) {
            log.error("Failed to unlock user [{}] in Redis", userId, ex);
        }
    }
}
