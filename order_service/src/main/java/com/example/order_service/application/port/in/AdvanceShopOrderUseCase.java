package com.example.order_service.application.port.in;

import com.example.order_service.domain.model.OrderStatus;

public interface AdvanceShopOrderUseCase {
    void advanceShopOrder(Long orderId, OrderStatus currentStatus);
}
