package com.example.common.security;

// Auth và Gateway phải dùng cùng quy ước key để đọc đúng trạng thái thu hồi và khóa.
public final class AccessControlCacheKeys {

    private static final String PREFIX = "auth";

    private AccessControlCacheKeys() {
    }

    public static String blacklistJwt(String jwt) {
        return PREFIX + ":blacklist:jwt:" + jwt;
    }

    public static String userLocked(String userId) {
        return PREFIX + ":user:locked:" + userId;
    }
}
