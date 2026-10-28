package com.example.producr_service.application.dto.response;

import java.time.LocalDateTime;

public record CompletedFlashSaleCampaignView(Long id, String name, LocalDateTime startsAt, LocalDateTime endsAt) {
}
