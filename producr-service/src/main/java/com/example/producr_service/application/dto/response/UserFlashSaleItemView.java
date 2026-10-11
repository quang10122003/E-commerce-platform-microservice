package com.example.producr_service.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// Dữ liệu hiện tại của variant và hạn mức sale để dựng card sale
public record UserFlashSaleItemView(
        VariantCartView variant,
        BigDecimal discountPercent,
        int registeredQuantity,
        long soldQuantity,
        // Giờ lưu trong DB; khi trả API quy đổi sang offset bằng múi giờ của server .
        LocalDateTime endsAt
) {}
