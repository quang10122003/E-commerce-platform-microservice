package com.example.producr_service.adapter.out.persistence.inbox;

import com.example.producr_service.application.port.out.repo.InboxEventPort;
import com.example.producr_service.adapter.entity.InboxEventEntity;
import com.example.producr_service.adapter.mapper.InboxEventMapper;
import com.example.producr_service.domain.model.InboxEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
    public class InboxEventAdapter implements InboxEventPort {
    private final InboxEventJpa inboxEventJpa;
    private final InboxEventMapper inboxEventMapper;

    @Override
    public boolean tryInsert(InboxEvent event) {
        InboxEventEntity entity = inboxEventMapper.toEntity(event);
        inboxEventJpa.insertIfAbsent(entity.getEventId(), entity.getEventType(),
                entity.getPayload(), entity.getProcessedAt());
        return inboxEventJpa.lastInsertedId() > 0;
    }
}
