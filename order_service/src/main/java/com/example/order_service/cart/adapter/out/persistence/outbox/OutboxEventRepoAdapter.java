package com.example.order_service.cart.adapter.out.persistence.outbox;

import com.example.order_service.cart.adapter.mapper.OutboxEventMapper;
import com.example.order_service.cart.application.Constant.Constant;
import com.example.order_service.cart.application.dto.outbox.OutboxEventDto;
import com.example.order_service.cart.application.port.out.outbox.OutboxPort;
import com.example.order_service.cart.domain.model.OutboxEvent;
import com.example.order_service.cart.domain.model.OutboxStatus;
import lombok.RequiredArgsConstructor;
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
    private final Clock clock;

    @Override
    public void save(OutboxEventDto event) {
        outboxEventJpa.save(outboxEventMapper.toEntity(event));
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
