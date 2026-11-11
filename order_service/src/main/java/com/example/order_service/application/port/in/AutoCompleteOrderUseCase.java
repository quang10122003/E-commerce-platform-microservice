package com.example.order_service.application.port.in;

import java.util.List;

public interface AutoCompleteOrderUseCase {
    List<Long> findDueOrderIds();
    void complete(Long orderId);
}
