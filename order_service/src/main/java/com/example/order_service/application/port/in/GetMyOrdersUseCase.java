package com.example.order_service.application.port.in;

import com.example.order_service.application.dto.response.UserOrderResponse;
import com.example.order_service.domain.model.OrderStatus;
import com.example.common.response.PageResponse;
import com.example.common.response.PageQuery;

public interface GetMyOrdersUseCase {
    PageResponse<UserOrderResponse> getMyOrders(OrderStatus status, String keyword, PageQuery pageQuery);
}
