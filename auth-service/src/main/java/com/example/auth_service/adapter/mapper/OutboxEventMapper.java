package com.example.auth_service.adapter.mapper;

import org.springframework.stereotype.Component;

import com.example.auth_service.adapter.entity.OutboxEventEntity;
import com.example.auth_service.application.DTO.OutboxEventDto;
import com.example.auth_service.domain.model.OutboxEvent;
import com.example.common.untill.JsonUtils;

@Component
public class OutboxEventMapper {

    private final JsonUtils jsonUtils;

    public OutboxEventMapper(JsonUtils jsonUtils) {
        this.jsonUtils = jsonUtils;
    }

    // Chuyển domain event sang entity để lưu payload JSON xuống database.
    public OutboxEventEntity toEntity(OutboxEvent event, String payload) {
        OutboxEventEntity entity = new OutboxEventEntity();
        entity.setEventId(event.eventId().toString());
        entity.setAggregateType(event.aggregateType());
        entity.setAggregateId(event.aggregateId());
        entity.setEventType(event.eventType().getValue());
        entity.setPayload(payload);
        entity.setStatus(OutboxEventEntity.Status.valueOf(event.status().name()));
        entity.setRetryCount(event.retryCount());
        entity.setMaxRetry(event.maxRetry());
        entity.setCreatedAt(event.createdAt());
        entity.setPublishedAt(event.publishedAt());
        entity.setErrorMessage(event.errorMessage());
        return entity;
    }

    // Chuyển entity sang DTO và deserialize payload thành object.
    public OutboxEventDto toDto(OutboxEventEntity entity) {
        return new OutboxEventDto(
                java.util.UUID.fromString(entity.getEventId()),
                entity.getAggregateType(),
                entity.getAggregateId(),
                entity.getEventType(),
                jsonUtils.fromJson(entity.getPayload()),
                OutboxEvent.Status.valueOf(entity.getStatus().name()),
                entity.getRetryCount(),
                entity.getMaxRetry(),
                entity.getCreatedAt(),
                entity.getPublishedAt(),
                entity.getErrorMessage());
    }

}
