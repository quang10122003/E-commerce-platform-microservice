package com.example.auth_service.adapter.out.cache.key;

import com.example.common.security.AccessControlCacheKeys;

public final class AuthCacheKeys {


    private AuthCacheKeys() {}

    public static String blacklistJwt(String jwt) {
        return AccessControlCacheKeys.blacklistJwt(jwt);
    }

    public static String userLocked(String userId) {
        return AccessControlCacheKeys.userLocked(userId);
    }


}
