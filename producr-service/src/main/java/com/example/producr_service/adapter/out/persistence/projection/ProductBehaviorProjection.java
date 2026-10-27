package com.example.producr_service.adapter.out.persistence.projection;

import com.example.producr_service.domain.model.BehaviorType;

import java.time.LocalDateTime;

public interface ProductBehaviorProjection {
    String getEventId();

    Long getUserId();

    Long getProductId();

    BehaviorType getEventType();

    LocalDateTime getCreatedAt();
}
