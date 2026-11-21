package com.example.order_service.adapter.out.persistence.projection;

import com.example.order_service.domain.model.OrderStatus;

public interface StatusCountProjection {
    OrderStatus getStatus();
    long getTotal();
}
