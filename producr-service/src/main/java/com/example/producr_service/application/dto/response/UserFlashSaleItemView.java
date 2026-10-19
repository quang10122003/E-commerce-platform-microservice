package com.example.producr_service.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record UserFlashSaleItemView(
        VariantCartView variant,
        BigDecimal discountPercent,
        int registeredQuantity,
        long soldQuantity,
        // Thời gian DB chưa có offset, phải dùng múi giờ lưu trữ khi trả API.
        LocalDateTime endsAt
) {}
