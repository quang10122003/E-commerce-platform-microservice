package com.example.order_service.order.domain.model;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public final class InboxEvent {

    private final Long id;
    private final UUID eventId;
    private final String eventType;
    private final String payload;
    private final LocalDateTime processedAt;

    private InboxEvent(Long id, UUID eventId, String eventType, String payload, LocalDateTime processedAt) {
        if (id != null && id <= 0) {
            throw new IllegalArgumentException("ID Inbox không hợp lệ");
        }
        if (eventType == null || eventType.isBlank() || eventType.length() > 100) {
            throw new IllegalArgumentException("Loại event Inbox không hợp lệ");
        }
        if (payload == null || payload.isBlank()) {
            throw new IllegalArgumentException("Payload Inbox không được để trống");
        }
        this.id = id;
        this.eventId = Objects.requireNonNull(eventId, "Event ID Inbox không được null");
        this.eventType = eventType;
        this.payload = payload;
        this.processedAt = Objects.requireNonNull(processedAt, "Thời điểm xử lý Inbox không được null");
    }

    public static InboxEvent create(UUID eventId, String eventType, String payload, LocalDateTime processedAt) {
        return new InboxEvent(null, eventId, eventType, payload, processedAt);
    }

    public static InboxEvent reconstitute(Long id, UUID eventId, String eventType,
                                          String payload, LocalDateTime processedAt) {
        Objects.requireNonNull(id, "ID Inbox không được null khi khôi phục");
        return new InboxEvent(id, eventId, eventType, payload, processedAt);
    }

    public Long getId() {
        return id;
    }

    public UUID getEventId() {
        return eventId;
    }

    public String getEventType() {
        return eventType;
    }

    public String getPayload() {
        return payload;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }
}
