//cache key của auth servie
package com.example.auth_service.adapter.out.cache.key;

import com.example.common.security.AccessControlCacheKeys;

public final class AuthCacheKeys {


    private AuthCacheKeys() {}

    // --- Access control ---
    public static String blacklistJwt(String jwt) {
        return AccessControlCacheKeys.blacklistJwt(jwt);
    }

    // key cuả user bị lock
    public static String userLocked(String userId) {
        return AccessControlCacheKeys.userLocked(userId);
    }


//    public static String userProfile(String userId) {
//        return PREFIX + ":cache:user_profile:" + userId;
//    }
}
