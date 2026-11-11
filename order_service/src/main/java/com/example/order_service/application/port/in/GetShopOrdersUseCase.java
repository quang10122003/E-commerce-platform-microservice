package com.example.order_service.application.port.in;

import com.example.order_service.application.dto.response.ShopOrderResponse;
import com.example.order_service.application.dto.request.ShopOrderFilter;

import java.util.List;

public interface GetShopOrdersUseCase {
    List<ShopOrderResponse> getShopOrders(ShopOrderFilter filter);
}
