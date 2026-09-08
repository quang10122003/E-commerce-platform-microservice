package com.example.auth_service.application.port.out;

import java.time.Duration;

// định nghãi các hàm cache cho việc AccessControl
public interface AccessControlCachePort {
    // lock check lock jwt
    void blacklistToken(String jwt,Duration ttl);
    boolean isTokenBlacklisted(String jwt);

    // các hàm liên quan lock check lock user
    void lockUser(String userId);
    boolean isUserLocked(String userId);
    void unlockUser(String userId);
}
