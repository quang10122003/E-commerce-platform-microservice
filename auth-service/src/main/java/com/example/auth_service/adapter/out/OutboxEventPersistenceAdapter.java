package com.example.auth_service.adapter.out;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
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
public class OutboxEventPersistenceAdapter implements OutboxEventPort {

    private final OutboxEventRepoJpa outboxEventRepoJpa;
    private final OutboxEventMapper outboxEventMapper;
    private final JsonUtils jsonUtils;

    @Override
    public void save(OutboxEvent event) {
        String payload = jsonUtils.toJson(event.payload());
        // đẩy lên outbox 
        outboxEventRepoJpa.save(outboxEventMapper.toEntity(event, payload));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OutboxEventDto> findPending() {
        return outboxEventRepoJpa.findByStatusOrderByCreatedAtAsc(
                        OutboxEventEntity.Status.PENDING)
                .stream()
                .map(outboxEventMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public void markPublished(UUID eventId) {
        outboxEventRepoJpa.findByEventId(eventId.toString()).ifPresent(event -> {
            event.setStatus(OutboxEventEntity.Status.PUBLISHED);
            event.setPublishedAt(LocalDateTime.now());
            event.setErrorMessage(null);
        });
    }

    @Override
    @Transactional
    public void markFailed(UUID eventId, String errorMessage) {
        outboxEventRepoJpa.findByEventId(eventId.toString()).ifPresent(event -> {
            int retryCount = event.getRetryCount() + 1;
            event.setRetryCount(retryCount);
            event.setErrorMessage(errorMessage);
            if (retryCount >= event.getMaxRetry()) {
                event.setStatus(OutboxEventEntity.Status.FAILED);
            }
        });
    }

}
