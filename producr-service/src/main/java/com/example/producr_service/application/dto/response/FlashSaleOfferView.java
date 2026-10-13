package com.example.producr_service.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// Hạn mức và ưu đãi của một item sale, không tải ảnh hoặc thuộc tính.
public record FlashSaleOfferView(BigDecimal discountPercent, int registeredQuantity,
                                 long soldQuantity, LocalDateTime endsAt) {}
