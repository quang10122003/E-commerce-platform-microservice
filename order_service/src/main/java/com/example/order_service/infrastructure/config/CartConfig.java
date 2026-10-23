package com.example.order_service.infrastructure.config;

import com.example.order_service.cart.application.port.in.AddCartItemUseCase;
import com.example.order_service.cart.application.port.in.GetCartUseCase;
import com.example.order_service.cart.application.port.in.GetCountItemCartUseCase;
import com.example.order_service.cart.application.port.in.RemoveCartItemUseCase;
import com.example.order_service.cart.application.port.in.UpdateCartItemQuantityUseCase;
import com.example.order_service.cart.application.port.out.client.CurrentUserPort;
import com.example.order_service.cart.application.port.out.client.ProductClientPort;
import com.example.order_service.cart.application.port.out.repo.CartRepoPort;
import com.example.order_service.cart.application.service.CartService;
import com.example.order_service.cart.application.port.out.outbox.OutboxPort;
import com.example.order_service.cart.application.port.in.ProcessProductEventUseCase;
import com.example.order_service.cart.application.port.out.objettMapper.ObjectMapperPort;
import com.example.order_service.cart.application.registry.KafkaEventHandlerRegistry;
import com.example.order_service.cart.application.service.ProductEventApplicationService;
import com.example.order_service.cart.application.strategy.eventKafka.EventHandler;
import com.example.order_service.cart.application.strategy.eventKafka.ProductDeleteEventHandler;
import com.example.order_service.cart.application.strategy.eventKafka.ProductvariantDeleteEventHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.util.List;

@Configuration
public class CartConfig {
    @Bean
    CartService cartService(
            CurrentUserPort currentUserPort,
            CartRepoPort cartRepoPort,
            ProductClientPort productClientPort,
            Clock clock, OutboxPort outboxPort) {
        return new CartService(currentUserPort, cartRepoPort, productClientPort, clock, outboxPort);
    }

    @Bean
    ProductDeleteEventHandler productDeleteEventHandler(ObjectMapperPort objectMapperPort, CartService cartService) {
        return new ProductDeleteEventHandler(objectMapperPort, cartService);
    }

    @Bean
    ProductvariantDeleteEventHandler productvariantDeleteEventHandler(ObjectMapperPort objectMapperPort, CartService cartService) {
        return new ProductvariantDeleteEventHandler(objectMapperPort, cartService);
    }

    @Bean
    KafkaEventHandlerRegistry productEventHandlerRegistry(List<EventHandler> eventHandlers) {
        return new KafkaEventHandlerRegistry(eventHandlers);
    }

    @Bean
    ProcessProductEventUseCase processProductEventUseCase(KafkaEventHandlerRegistry eventHandlerRegistry) {
        return new ProductEventApplicationService(eventHandlerRegistry);
    }

    @Bean
    GetCartUseCase getCartUseCase(CartService cartService) {
        return cartService;
    }

    @Bean
    GetCountItemCartUseCase getCountItemCartUseCase(CartService cartService) {
        return cartService;
    }

    @Bean
    AddCartItemUseCase addCartItemUseCase(CartService cartService) {
        return cartService;
    }

    @Bean
    UpdateCartItemQuantityUseCase updateCartItemQuantityUseCase(CartService cartService) {
        return cartService;
    }

    @Bean
    RemoveCartItemUseCase removeCartItemUseCase(CartService cartService) {
        return cartService;
    }
}
