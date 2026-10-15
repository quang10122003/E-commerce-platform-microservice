package com.example.producr_service.adapter.mapper;

import com.example.producr_service.adapter.entity.OutboxEventEntity;
import com.example.producr_service.application.dto.outbox.OutboxEventDto;
import com.example.producr_service.domain.model.OutboxStatus;
import com.example.common.untill.JsonUtils;
import org.springframework.stereotype.Component;

import java.util.UUID;

// Chuyển đổi DTO outbox và entity persistence, xử lý JSON tại adapter.
@Component
public class OutboxEventMapper {

    private final JsonUtils jsonUtils;

    // Nhận utility JSON dùng chung để đọc payload từ database.
    public OutboxEventMapper(JsonUtils jsonUtils) {
        this.jsonUtils = jsonUtils;
    }

    // Chuyển DTO outbox thành entity để lưu xuống database.
    public OutboxEventEntity toEntity(OutboxEventDto event, String payload) {
        if (event == null) {
            return null;
        }
        OutboxEventEntity entity = new OutboxEventEntity();
        entity.setEventId(map(event.eventId()));
        entity.setAggregateType(event.aggregateType());
        entity.setAggregateId(event.aggregateId());
        entity.setEventType(event.eventType());
        entity.setPayload(payload);
        entity.setStatus(map(event.status()));
        entity.setRetryCount(event.retryCount());
        entity.setMaxRetry(event.maxRetry());
        entity.setCreatedAt(event.createdAt());
        entity.setNextAttemptAt(event.nextAttemptAt());
        entity.setPublishedAt(event.publishedAt());
        entity.setErrorMessage(event.errorMessage());
        return entity;
    }

    // Chuyển entity persistence thành DTO để application xử lý.
    public OutboxEventDto toDto(OutboxEventEntity entity) {
        if (entity == null) {
            return null;
        }
        return new OutboxEventDto(
                map(entity.getEventId()),
                entity.getAggregateType(),
                entity.getAggregateId(),
                entity.getEventType(),
                jsonUtils.fromJson(entity.getPayload()),
                map(entity.getStatus()),
                entity.getRetryCount(),
                entity.getMaxRetry(),
                entity.getCreatedAt(),
                entity.getNextAttemptAt(),
                entity.getPublishedAt(),
                entity.getErrorMessage()
        );
    }

    // Chuyển UUID thành chuỗi phù hợp với cột VARCHAR trong database.
    public String map(UUID value) {
        return value == null ? null : value.toString();
    }

    // Chuyển chuỗi từ database thành UUID của DTO.
    public UUID map(String value) {
        return value == null ? null : UUID.fromString(value);
    }

    // Chuyển trạng thái domain sang trạng thái persistence.
    public OutboxEventEntity.Status map(OutboxStatus value) {
        return value == null ? null : OutboxEventEntity.Status.valueOf(value.name());
    }

    // Chuyển trạng thái persistence sang trạng thái domain.
    public OutboxStatus map(OutboxEventEntity.Status value) {
        return value == null ? null : OutboxStatus.valueOf(value.name());
    }
}
