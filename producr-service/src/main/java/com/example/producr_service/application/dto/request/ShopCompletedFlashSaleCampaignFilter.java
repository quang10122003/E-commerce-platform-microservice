package com.example.producr_service.application.dto.request;

import java.time.OffsetDateTime;

public record ShopCompletedFlashSaleCampaignFilter(int page, int size, OffsetDateTime from,
                                                  OffsetDateTime to, String keyword) {
}
