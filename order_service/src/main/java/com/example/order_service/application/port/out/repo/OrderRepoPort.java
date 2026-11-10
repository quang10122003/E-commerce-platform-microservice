package com.example.order_service.application.port.out.repo;

import com.example.order_service.domain.model.Order;
import java.util.List;

public interface OrderRepoPort {
    List<Long> saveAll(List<Order> orders);
    List<Order> findByCheckoutIdForUpdate(Long checkoutId);
    void saveStatus(List<Order> orders);
}
