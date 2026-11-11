package com.example.order_service.application.port.in;

public interface CheckUserActiveOrdersUseCase {

    boolean hasActiveOrders(Long userId);
}
