package com.example.order_service.infrastructure.config;

import com.example.order_service.cart.application.port.in.GetCartUseCase;
import com.example.order_service.cart.application.port.out.client.CurrentUserPort;
import com.example.order_service.cart.application.port.out.client.ProductClientPort;
import com.example.order_service.cart.application.port.out.repo.CartRepoPort;
import com.example.order_service.cart.application.service.CartService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CartConfig {
    // Đăng ký use case giỏ hàng thông qua input port.
    @Bean
    GetCartUseCase getCartUseCase(
            CurrentUserPort currentUserPort,
            CartRepoPort cartRepoPort,
            ProductClientPort productClientPort) {
        return new CartService(currentUserPort, cartRepoPort, productClientPort);
    }
}
