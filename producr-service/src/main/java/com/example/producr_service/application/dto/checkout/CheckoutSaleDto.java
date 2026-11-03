package com.example.producr_service.application.dto.checkout;

import com.example.producr_service.domain.model.FlashSaleCampaignStatus;
import com.example.producr_service.domain.model.FlashSaleItemStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CheckoutSaleDto(Long id, Long campaignId, Long variantId, BigDecimal discountPercent,
                              int registeredQuantity, long soldQuantity, FlashSaleItemStatus status,
                              FlashSaleCampaignStatus campaignStatus, LocalDateTime startsAt, LocalDateTime endsAt) {
}
