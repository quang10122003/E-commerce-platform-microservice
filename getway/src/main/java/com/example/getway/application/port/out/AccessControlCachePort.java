package com.example.getway.application.port.out;

public interface AccessControlCachePort {

    boolean isTokenBlacklisted(String token);

    boolean isUserLocked(String userId);
}
