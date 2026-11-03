package com.example.notification.application.port.out;

import java.util.UUID;

public interface NotificationStreamPort {
    void publish(String key, UUID eventId, String eventType, Object data);
}
