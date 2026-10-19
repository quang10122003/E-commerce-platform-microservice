package com.example.notification.adapter.out.cache.key;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public final class NotificationCacheKeys {
    private static final String PREFIX = "notification";

    private NotificationCacheKeys() {
    }

    public static String createWebSocketTicketKey(String ticket) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(ticket.getBytes(StandardCharsets.UTF_8));
            return PREFIX + ":ws-ticket:" + HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("Môi trường không hỗ trợ SHA-256", exception);
        }
    }
}
