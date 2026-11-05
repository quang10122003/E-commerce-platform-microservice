package com.example.auth_service.application.constant;

public final class Constant {
    public static final String ACCESS_TOKEN = "access";
    public static final String REFRESH_TOKEN = "refresh";
    public static final String USER = "User";
    public static final int OUTBOX_BATCH_SIZE = 100;
    public static final String AUTH_EVENTS_TOPIC = "auth-service.events";

    private Constant() {
    }
}
