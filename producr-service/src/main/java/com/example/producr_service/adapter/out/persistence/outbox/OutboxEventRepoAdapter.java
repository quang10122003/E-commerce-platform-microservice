package com.example.producr_service.adapter.out.persistence.outbox;

import com.example.producr_service.adapter.entity.OutboxEventEntity;
import com.example.producr_service.adapter.mapper.OutboxEventMapper;
import com.example.producr_service.application.dto.outbox.OutboxEventDto;
import com.example.producr_service.application.port.out.outbox.OutboxPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

// Adapter persistence quản lý vòng đời event outbox tổng quát.
@Repository
@RequiredArgsConstructor
public class OutboxEventRepoAdapter implements OutboxPort {

    private final OutboxEventJpa outboxEventJpa;
    private final OutboxEventMapper outboxEventMapper;

    @Override
    public void save(OutboxEventDto event) {
        outboxEventJpa.save(outboxEventMapper.toEntity(event));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OutboxEventDto> findPending() {
        return outboxEventJpa
                .findByStatusAndNextAttemptAtLessThanEqualOrderByCreatedAtAsc(
                        OutboxEventEntity.Status.PENDING,
                        LocalDateTime.now()
                )
                .stream()
                .map(outboxEventMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public void markPublished(UUID eventId) {
        outboxEventJpa.findByEventId(eventId.toString()).ifPresent(event -> {
            event.setStatus(OutboxEventEntity.Status.PUBLISHED);
            event.setPublishedAt(LocalDateTime.now());
            event.setErrorMessage(null);
        });
    }

    @Override
    @Transactional
    public void markFailed(UUID eventId, String errorMessage) {
        outboxEventJpa.findByEventId(eventId.toString()).ifPresent(event -> {
            int retryCount = event.getRetryCount() + 1;
            event.setRetryCount(retryCount);
            event.setErrorMessage(errorMessage);

            // Tăng dần thời gian chờ để tránh dồn tải khi hệ thống đích đang lỗi.
            if (retryCount >= event.getMaxRetry()) {
                event.setStatus(OutboxEventEntity.Status.FAILED);
            } else {
                long delaySeconds = Math.min(300L, 5L * (1L << Math.min(retryCount - 1, 5)));
                event.setNextAttemptAt(LocalDateTime.now().plusSeconds(delaySeconds));
            }
        });
    }
}
