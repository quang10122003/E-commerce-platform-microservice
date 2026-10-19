package com.example.producr_service.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record FlashSaleOfferView(Long variantId, BigDecimal discountPercent, int registeredQuantity,
                                 long soldQuantity, LocalDateTime endsAt) {}
