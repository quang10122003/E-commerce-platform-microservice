package com.example.order_service.cart.application.port.out.client;

import com.example.order_service.cart.application.dto.repone.UserInternaInfoRespone;

public interface CurrentUserPort {
    UserInternaInfoRespone getInfo();
}
