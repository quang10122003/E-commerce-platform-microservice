package com.example.order_service.cart.adapter.mapper;

import com.example.order_service.cart.application.port.out.objettMapper.ObjectMapperPort;
import com.example.order_service.cart.adapter.entity.OutboxEventEntity;
import com.example.order_service.cart.application.dto.outbox.OutboxEventDto;
import com.example.order_service.cart.domain.model.OutboxEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OutboxEventMapper {
    private final ObjectMapperPort objectMapperPort;

    public OutboxEventEntity toEntity(OutboxEventDto event) {
        OutboxEventEntity entity = new OutboxEventEntity();
        entity.setEventId(event.eventId().toString());
        entity.setAggregateType(event.aggregateType());
        entity.setAggregateId(event.aggregateId());
        entity.setEventType(event.eventType());
        entity.setPayload(objectMapperPort.writeValue(event.payload()));
        entity.setStatus(event.status());
        entity.setRetryCount(event.retryCount());
        entity.setMaxRetry(event.maxRetry());
        entity.setCreatedAt(event.createdAt());
        entity.setNextAttemptAt(event.nextAttemptAt());
        entity.setPublishedAt(event.publishedAt());
        entity.setErrorMessage(event.errorMessage());
        return entity;
    }

    public OutboxEventDto toDto(OutboxEventEntity entity) {
        return new OutboxEventDto(UUID.fromString(entity.getEventId()), entity.getAggregateType(),
                entity.getAggregateId(), entity.getEventType(), objectMapperPort.readValue(entity.getPayload(), Object.class),
                entity.getStatus(), entity.getRetryCount(), entity.getMaxRetry(), entity.getCreatedAt(),
                entity.getNextAttemptAt(), entity.getPublishedAt(), entity.getErrorMessage());
    }

    public OutboxEvent toDomain(OutboxEventEntity entity) {
        return new OutboxEvent(UUID.fromString(entity.getEventId()), entity.getAggregateType(),
                entity.getAggregateId(), entity.getEventType(), entity.getPayload(), entity.getStatus(),
                entity.getRetryCount(), entity.getMaxRetry(), entity.getCreatedAt(),
                entity.getNextAttemptAt(), entity.getPublishedAt(), entity.getErrorMessage());
    }

    public void updateState(OutboxEvent event, OutboxEventEntity entity) {
        entity.setStatus(event.getStatus());
        entity.setRetryCount(event.getRetryCount());
        entity.setNextAttemptAt(event.getNextAttemptAt());
        entity.setPublishedAt(event.getPublishedAt());
        entity.setErrorMessage(event.getErrorMessage());
    }
}
