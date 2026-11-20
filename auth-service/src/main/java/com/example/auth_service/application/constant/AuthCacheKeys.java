package com.example.auth_service.application.constant;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public final class AuthCacheKeys {
    private static final String PREFIX = "auth";

    private AuthCacheKeys() {
    }

    // Băm mã để người đọc được Redis cũng không dùng lại được mã đăng nhập còn hạn.
    public static String oauthLoginCode(String code) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(code.getBytes(StandardCharsets.UTF_8));
            return PREFIX + ":oauth2:login-code:" + HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("Môi trường không hỗ trợ SHA-256", exception);
        }
    }
}
