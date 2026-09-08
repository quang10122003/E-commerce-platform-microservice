package com.example.auth_service.adapter.out.persistence.outbox;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.auth_service.adapter.entity.OutboxEventEntity;

public interface OutboxEventRepoJpaJpa extends JpaRepository<OutboxEventEntity, Long> {

    // Lấy event theo thứ tự 
    List<OutboxEventEntity> findByStatusOrderByCreatedAtAsc(
            OutboxEventEntity.Status status);

    Optional<OutboxEventEntity> findByEventId(String eventId);
}
