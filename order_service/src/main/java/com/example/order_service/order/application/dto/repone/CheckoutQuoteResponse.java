package com.example.order_service.order.application.dto.repone;

import java.math.BigDecimal;

public record CheckoutQuoteResponse(BigDecimal totalAmount) {
}
