package com.example.order_service.application.port.in;

import java.util.List;
import com.example.order_service.application.dto.response.UserOrderResponse;
import com.example.order_service.domain.model.OrderStatus;

public interface GetMyOrdersUseCase {
    List<UserOrderResponse> getMyOrders(OrderStatus status);
}
