package com.example.auth_service.application.port.out;

public interface OrderActivityPort {

    boolean hasActiveOrders(Long userId);
}
