package com.example.producr_service.adapter.mapper;

import com.example.producr_service.adapter.entity.OutboxEventEntity;
import com.example.producr_service.application.dto.outbox.OutboxEventDto;
import com.example.producr_service.domain.model.OutboxStatus;
import com.example.common.untill.JsonUtils;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class OutboxEventMapper {

    private final JsonUtils jsonUtils;

    public OutboxEventMapper(JsonUtils jsonUtils) {
        this.jsonUtils = jsonUtils;
    }

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

    public String map(UUID value) {
        return value == null ? null : value.toString();
    }

    public UUID map(String value) {
        return value == null ? null : UUID.fromString(value);
    }

    public OutboxEventEntity.Status map(OutboxStatus value) {
        return value == null ? null : OutboxEventEntity.Status.valueOf(value.name());
    }

    public OutboxStatus map(OutboxEventEntity.Status value) {
        return value == null ? null : OutboxStatus.valueOf(value.name());
    }
}
