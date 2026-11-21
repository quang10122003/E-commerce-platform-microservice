package com.example.order_service.infrastructure.config;

import com.example.order_service.application.service.ShopAvailabilityService;

import com.example.order_service.application.strategy.EventHandler;
import com.example.order_service.application.port.in.AddCartItemUseCase;
import com.example.order_service.application.port.in.GetCartUseCase;
import com.example.order_service.application.port.in.GetCartQuoteUseCase;
import com.example.order_service.application.port.in.GetCountItemCartUseCase;
import com.example.order_service.application.port.in.RemoveCartItemUseCase;
import com.example.order_service.application.port.in.UpdateCartItemQuantityUseCase;
import com.example.order_service.application.port.out.client.CurrentUserPort;
import com.example.order_service.application.port.out.client.ProductClientPort;
import com.example.order_service.application.port.out.repo.CartRepoPort;
import com.example.order_service.application.service.CartService;
import com.example.order_service.application.port.out.outbox.OutboxPort;
import com.example.order_service.application.port.out.serialization.ObjectMapperPort;
import com.example.order_service.application.registry.KafkaEventHandlerRegistry;
import com.example.order_service.application.service.KafkaEventApplicationService;
import com.example.order_service.application.port.out.repo.OrderRepoPort;
import com.example.order_service.application.port.out.repo.ProductReviewRepoPort;
import com.example.order_service.application.service.OrderItemReferenceService;
import com.example.order_service.application.strategy.eventKafka.ProductDeleteEventHandler;
import com.example.order_service.application.strategy.eventKafka.UserDeletedEventHandler;
import com.example.order_service.application.service.UserAccountService;
import com.example.order_service.application.port.in.CheckUserActiveOrdersUseCase;
import com.example.order_service.application.port.out.repo.CartRepoPort;
import com.example.order_service.application.port.out.repo.CheckoutRepoPort;
import com.example.order_service.application.strategy.eventKafka.ProductvariantDeleteEventHandler;
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
            Clock clock, OutboxPort outboxPort,
            ShopAvailabilityService shopAvailabilityService) {
        return new CartService(currentUserPort, cartRepoPort, productClientPort, clock, outboxPort, shopAvailabilityService);
    }

    @Bean
    ProductDeleteEventHandler productDeleteEventHandler(ObjectMapperPort objectMapperPort, CartService cartService,
                                                        OrderItemReferenceService orderItemReferenceService) {
        return new ProductDeleteEventHandler(objectMapperPort, cartService, orderItemReferenceService);
    }

    @Bean
    ProductvariantDeleteEventHandler productvariantDeleteEventHandler(ObjectMapperPort objectMapperPort, CartService cartService,
                                                                      OrderItemReferenceService orderItemReferenceService) {
        return new ProductvariantDeleteEventHandler(objectMapperPort, cartService, orderItemReferenceService);
    }

    @Bean
    UserAccountService userAccountService(OrderRepoPort orderRepoPort, CheckoutRepoPort checkoutRepoPort,
                                          CartRepoPort cartRepoPort) {
        return new UserAccountService(orderRepoPort, checkoutRepoPort, cartRepoPort);
    }

    @Bean
    CheckUserActiveOrdersUseCase checkUserActiveOrdersUseCase(UserAccountService userAccountService) {
        return userAccountService;
    }

    @Bean
    UserDeletedEventHandler userDeletedEventHandler(ObjectMapperPort objectMapperPort, UserAccountService userAccountService) {
        return new UserDeletedEventHandler(objectMapperPort, userAccountService);
    }

    @Bean
    OrderItemReferenceService orderItemReferenceService(OrderRepoPort orderRepoPort,
                                                        ProductReviewRepoPort productReviewRepoPort) {
        return new OrderItemReferenceService(orderRepoPort, productReviewRepoPort);
    }

    @Bean
    KafkaEventHandlerRegistry kafkaEventHandlerRegistry(List<EventHandler<String>> eventHandlers) {
        return new KafkaEventHandlerRegistry(eventHandlers);
    }

    @Bean(name = {"kafkaEventApplicationService", "processAuthEventUseCase", "processCheckoutEventUseCase",
            "processProductEventUseCase"})
    KafkaEventApplicationService kafkaEventApplicationService(KafkaEventHandlerRegistry eventHandlerRegistry) {
        return new KafkaEventApplicationService(eventHandlerRegistry);
    }

    @Bean
    GetCartUseCase getCartUseCase(CartService cartService) {
        return cartService;
    }

    @Bean
    GetCartQuoteUseCase getCartQuoteUseCase(CartService cartService) {
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
