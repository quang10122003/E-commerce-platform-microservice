package com.example.order_service.order.application.port.out.repo;

import com.example.order_service.order.domain.model.Order;
import java.util.List;

public interface OrderRepoPort {
    List<Long> saveAll(List<Order> orders);
}
