package com.example.producr_service.application.dto.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record ShopLockChangedEvent(UUID eventId, String eventType, Long shopId, boolean locked,
                                   LocalDateTime occurredAt) {
    public static final String EVENT_TYPE = "SHOP_LOCK_CHANGED";
}
