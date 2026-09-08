package com.example.common.security;

// Tập trung quy ước key Redis dùng chung giữa auth-service và Gateway.
public final class AccessControlCacheKeys {

    private static final String PREFIX = "auth";

    private AccessControlCacheKeys() {
    }

    // Tạo key blacklist cho JWT đã bị thu hồi.
    public static String blacklistJwt(String jwt) {
        return PREFIX + ":blacklist:jwt:" + jwt;
    }

    // Tạo key trạng thái khóa của user.
    public static String userLocked(String userId) {
        return PREFIX + ":user:locked:" + userId;
    }
}
