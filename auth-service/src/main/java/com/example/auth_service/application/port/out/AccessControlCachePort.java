package com.example.auth_service.application.port.out;

import java.time.Duration;

public interface AccessControlCachePort {
    void blacklistToken(String jwt,Duration ttl);
    boolean isTokenBlacklisted(String jwt);

    void lockUser(String userId);
    boolean isUserLocked(String userId);
    void unlockUser(String userId);
}
