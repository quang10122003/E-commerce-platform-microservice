package com.example.order_service.application.port.out.client;

import com.example.order_service.application.dto.response.VnpayQueryResult;

import java.time.LocalDateTime;

public interface VnpayQueryPort {
    VnpayQueryResult queryTransaction(String transactionReference, LocalDateTime transactionDate,
                                      LocalDateTime now);
}
