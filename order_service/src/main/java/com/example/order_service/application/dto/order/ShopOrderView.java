package com.example.order_service.application.dto.order;

import com.example.order_service.application.dto.response.ShopOrderResponse;

public record ShopOrderView(UserOrderView order, ShopOrderResponse.Buyer buyer) {
}
