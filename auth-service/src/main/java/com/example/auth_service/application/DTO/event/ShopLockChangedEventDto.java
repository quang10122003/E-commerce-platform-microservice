package com.example.auth_service.application.DTO.event;

import java.time.LocalDateTime;
import java.util.UUID;

// occurredAt giúp consumer bỏ qua event cũ đến sau khi khóa/mở khóa liên tiếp.
public record ShopLockChangedEventDto(UUID eventId, String eventType, Long shopId, boolean locked,
                                      LocalDateTime occurredAt) {
    public static final String EVENT_TYPE = "SHOP_LOCK_CHANGED";
}
