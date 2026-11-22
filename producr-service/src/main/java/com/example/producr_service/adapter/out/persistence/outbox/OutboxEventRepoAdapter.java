package com.example.producr_service.adapter.out.persistence.outbox;

import com.example.producr_service.adapter.entity.OutboxEventEntity;
import com.example.producr_service.adapter.mapper.OutboxEventMapper;
import com.example.producr_service.application.dto.outbox.OutboxEventDto;
import com.example.producr_service.application.constant.Constant;
import com.example.producr_service.application.port.out.outbox.OutboxPort;
import com.example.common.untill.JsonUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.PageRequest;

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
    private final JsonUtils jsonUtils;
    private final Clock clock;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public Optional<OutboxEventDto> findByEventId(UUID eventId) {
        return outboxEventJpa.findByEventId(eventId.toString()).map(outboxEventMapper::toDto);
    }

    @Override
    public void save(OutboxEventDto event) {
        outboxEventJpa.save(outboxEventMapper.toEntity(event, jsonUtils.toJson(event.payload())));
        // Listener chỉ nhận event này sau khi transaction commit, nên không gửi Kafka cho dữ liệu có thể bị rollback.
        applicationEventPublisher.publishEvent(event);
    }

    @Override
    public List<OutboxEventDto> findPending() {
        return outboxEventJpa
                .findByStatusAndNextAttemptAtLessThanEqualOrderByIdAsc(
                        OutboxEventEntity.Status.PENDING,
                        LocalDateTime.now(clock), PageRequest.of(0, Constant.OUTBOX_BATCH_SIZE)
                )
                .stream()
                .map(outboxEventMapper::toDto)
                .toList();
    }

    @Override
    public Optional<OutboxEventDto> lockPendingEvent(UUID eventId) {
        return outboxEventJpa.lockPendingEvent(eventId.toString(), LocalDateTime.now(clock))
                .map(outboxEventMapper::toDto);
    }

    @Override
    public void markPublished(UUID eventId) {
        outboxEventJpa.findByEventIdForUpdate(eventId.toString()).ifPresent((OutboxEventEntity event) -> {
            if (event.getStatus() != OutboxEventEntity.Status.PENDING) {
                return;
            }
            event.setStatus(OutboxEventEntity.Status.PUBLISHED);
            event.setPublishedAt(LocalDateTime.now(clock));
            event.setErrorMessage(null);
        });
    }

    @Override
    public void markFailed(UUID eventId, String errorMessage) {
        outboxEventJpa.findByEventIdForUpdate(eventId.toString()).ifPresent((OutboxEventEntity event) -> {
            if (event.getStatus() != OutboxEventEntity.Status.PENDING) {
                return;
            }
            int retryCount = event.getRetryCount() + 1;
            event.setRetryCount(retryCount);
            event.setErrorMessage(errorMessage);

            // Tăng dần thời gian chờ để tránh dồn tải khi hệ thống đích đang lỗi.
            if (retryCount >= event.getMaxRetry()) {
                event.setStatus(OutboxEventEntity.Status.FAILED);
            } else {
                long delaySeconds = Math.min(300L, 5L * (1L << Math.min(retryCount - 1, 5)));
                event.setNextAttemptAt(LocalDateTime.now(clock).plusSeconds(delaySeconds));
            }
        });
    }
}
