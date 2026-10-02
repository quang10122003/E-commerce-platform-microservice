package com.example.order_service.cart.application.dto.request;

import java.util.List;

public record ProductCartInfoRequest(List<Long> variantIds) {
}
