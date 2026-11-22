package com.example.order_service.adapter.out.persistence.outbox;

import com.example.order_service.adapter.entity.OutboxEventEntity;
import com.example.order_service.adapter.mapper.OutboxEventMapper;
import com.example.order_service.application.constant.Constant;
import com.example.order_service.application.dto.event.CheckoutFailedPayload;
import com.example.order_service.application.dto.event.CheckoutSucceededPayload;
import com.example.order_service.application.port.out.serialization.ObjectMapperPort;
import com.example.order_service.application.dto.outbox.OutboxEventDto;
import com.example.order_service.application.port.out.outbox.OutboxPort;
import com.example.order_service.domain.model.OutboxEvent;
import com.example.order_service.domain.model.OutboxStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class OutboxEventRepoAdapter implements OutboxPort {
    private final OutboxEventJpa outboxEventJpa;
    private final OutboxEventMapper outboxEventMapper;
    private final ObjectMapperPort objectMapperPort;
    private final Clock clock;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public Optional<CheckoutSucceededPayload> findCheckoutSucceeded(Long checkoutId) {
        return outboxEventJpa.findFirstByAggregateTypeAndAggregateIdAndEventTypeOrderByIdDesc(
                        Constant.CHECKOUT, checkoutId.toString(), CheckoutSucceededPayload.EVENT_TYPE)
                .map((OutboxEventEntity entity) -> objectMapperPort.readValue(entity.getPayload(),
                        CheckoutSucceededPayload.class));
    }

    @Override
    public Optional<CheckoutFailedPayload> findCheckoutFailed(Long checkoutId) {
        return outboxEventJpa.findFirstByAggregateTypeAndAggregateIdAndEventTypeOrderByIdDesc(
                        Constant.CHECKOUT, checkoutId.toString(), CheckoutFailedPayload.EVENT_TYPE)
                .map((OutboxEventEntity entity) -> objectMapperPort.readValue(entity.getPayload(),
                        CheckoutFailedPayload.class));
    }

    @Override
    public void save(OutboxEventDto event) {
        outboxEventJpa.save(outboxEventMapper.toEntity(event));
        // Listener chỉ nhận event này sau khi transaction commit, nên không gửi Kafka cho dữ liệu có thể bị rollback.
        applicationEventPublisher.publishEvent(event);
    }

    @Override
    public List<OutboxEventDto> findPending() {
        return outboxEventJpa.findByStatusAndNextAttemptAtLessThanEqualOrderByIdAsc(
                        OutboxStatus.PENDING, LocalDateTime.now(clock), PageRequest.of(0, Constant.OUTBOX_BATCH_SIZE))
                .stream().map(outboxEventMapper::toDto).toList();
    }

    @Override
    public Optional<OutboxEventDto> lockPendingEvent(UUID eventId) {
        return outboxEventJpa.lockPendingEvent(eventId.toString(), LocalDateTime.now(clock))
                .map(outboxEventMapper::toDto);
    }

    @Override
    public void markPublished(UUID eventId) {
        outboxEventJpa.findByEventIdForUpdate(eventId.toString()).ifPresent(entity -> {
            OutboxEvent event = outboxEventMapper.toDomain(entity);
            event.markPublished(LocalDateTime.now(clock));
            outboxEventMapper.updateState(event, entity);
        });
    }

    @Override
    public void markFailed(UUID eventId, String errorMessage) {
        outboxEventJpa.findByEventIdForUpdate(eventId.toString()).ifPresent(entity -> {
            OutboxEvent event = outboxEventMapper.toDomain(entity);
            event.markFailed(LocalDateTime.now(clock), errorMessage);
            outboxEventMapper.updateState(event, entity);
        });
    }
}
