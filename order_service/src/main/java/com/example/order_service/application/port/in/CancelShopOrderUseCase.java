package com.example.order_service.application.port.in;

import com.example.order_service.domain.model.OrderCancelReason;

public interface CancelShopOrderUseCase {
    void cancelShopOrder(Long orderId, OrderCancelReason reason);
}
