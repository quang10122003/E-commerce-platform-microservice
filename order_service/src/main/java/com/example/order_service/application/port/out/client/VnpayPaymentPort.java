package com.example.order_service.application.port.out.client;

import com.example.order_service.application.dto.response.PaymentUrlResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface VnpayPaymentPort {
    PaymentUrlResponse createPaymentUrl(String transactionReference, BigDecimal amount,
                                       String clientIp, LocalDateTime now);
}
