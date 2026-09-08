package com.example.getway.application.port.out;

// Định nghĩa cổng đọc trạng thái thu hồi token và khóa user.
public interface AccessControlCachePort {

    // Kiểm tra token đã bị thu hồi hay chưa.
    boolean isTokenBlacklisted(String token);

    // Kiểm tra user đã bị khóa hay chưa.
    boolean isUserLocked(String userId);
}
