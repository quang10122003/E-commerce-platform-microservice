package com.example.order_service.application.dto.response;

import java.time.LocalDateTime;

public record PaymentUrlResponse(String url, LocalDateTime expiresAt) {
}
