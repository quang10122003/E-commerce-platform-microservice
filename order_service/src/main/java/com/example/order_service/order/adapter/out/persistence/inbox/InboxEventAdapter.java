package com.example.order_service.order.adapter.out.persistence.inbox;

import com.example.order_service.order.application.port.out.repo.InboxEventPort;
import com.example.order_service.order.adapter.entity.InboxEventEntity;
import com.example.order_service.order.adapter.mapper.InboxEventMapper;
import com.example.order_service.order.domain.model.InboxEvent;
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
