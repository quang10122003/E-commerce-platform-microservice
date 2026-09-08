package com.example.auth_service.adapter.out.persistence.outbox;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.auth_service.adapter.entity.OutboxEventEntity;
import com.example.auth_service.adapter.mapper.OutboxEventMapper;
import com.example.auth_service.application.DTO.OutboxEventDto;
import com.example.auth_service.application.port.out.OutboxEventPort;
import com.example.auth_service.domain.model.OutboxEvent;
import com.example.common.untill.JsonUtils;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class OutboxEventRepoAdapter implements OutboxEventPort {

    private final OutboxEventRepoJpaJpa outboxEventRepoJpaJpa;
    private final OutboxEventMapper outboxEventMapper;
    private final JsonUtils jsonUtils;

    @Override
    public void save(OutboxEvent event) {
        String payload = jsonUtils.toJson(event.payload());
        // đẩy lên outbox 
        outboxEventRepoJpaJpa.save(outboxEventMapper.toEntity(event, payload));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OutboxEventDto> findPending() {
        return outboxEventRepoJpaJpa.findByStatusOrderByCreatedAtAsc(
                        OutboxEventEntity.Status.PENDING)
                .stream()
                .map(outboxEventMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public void markPublished(UUID eventId) {
        outboxEventRepoJpaJpa.findByEventId(eventId.toString()).ifPresent(event -> {
            event.setStatus(OutboxEventEntity.Status.PUBLISHED);
            event.setPublishedAt(LocalDateTime.now());
            event.setErrorMessage(null);
        });
    }

    @Override
    @Transactional
    public void markFailed(UUID eventId, String errorMessage) {
        outboxEventRepoJpaJpa.findByEventId(eventId.toString()).ifPresent(event -> {
            int retryCount = event.getRetryCount() + 1;
            event.setRetryCount(retryCount);
            event.setErrorMessage(errorMessage);
            if (retryCount >= event.getMaxRetry()) {
                event.setStatus(OutboxEventEntity.Status.FAILED);
            }
        });
    }

}
