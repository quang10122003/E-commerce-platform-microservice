package com.example.order_service.cart.application.port.out.client;

import com.example.order_service.cart.application.dto.repone.CartInfoRepone;

import java.util.List;

public interface ProductClientPort {
    List<CartInfoRepone> getCartInfo(List<Long> variantIds);
}
