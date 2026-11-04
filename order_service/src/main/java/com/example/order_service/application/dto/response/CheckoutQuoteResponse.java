package com.example.order_service.application.dto.response;

import java.math.BigDecimal;

public record CheckoutQuoteResponse(BigDecimal totalAmount) {
}
