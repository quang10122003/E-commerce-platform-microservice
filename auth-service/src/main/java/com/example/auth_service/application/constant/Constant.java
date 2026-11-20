package com.example.auth_service.application.constant;

import java.time.Duration;

public final class Constant {
    public static final String ACCESS_TOKEN = "access";
    public static final String REFRESH_TOKEN = "refresh";
    public static final String USER = "User";
    public static final int OUTBOX_BATCH_SIZE = 100;
    public static final String AUTH_EVENTS_TOPIC = "auth-service.events";
    // Tên bucket lưu logo shop trên storage.
    public static final String AUTH_BUCKET = "auth";
    public static final String SHOP_LOGO_PREFIX = "ShopLogo";
    public static final String USER_AVATAR_PREFIX = "UserAvatar";
    // Đường dẫn API thao tác object trên Supabase Storage.
    public static final String STORAGE_OBJECT_PATH = "/storage/v1/object/";
    // Đường dẫn truy cập công khai tới object.
    public static final String STORAGE_PUBLIC_OBJECT_PATH = STORAGE_OBJECT_PATH + "public/";

    public static final Duration OAUTH_LOGIN_CODE_TTL = Duration.ofSeconds(60);

    private Constant() {
    }
}
