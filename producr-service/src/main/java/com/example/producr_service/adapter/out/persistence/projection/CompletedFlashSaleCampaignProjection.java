package com.example.producr_service.adapter.out.persistence.projection;

import java.time.LocalDateTime;

public interface CompletedFlashSaleCampaignProjection {
    Long getId();
    String getName();
    LocalDateTime getStartsAt();
    LocalDateTime getEndsAt();
}
