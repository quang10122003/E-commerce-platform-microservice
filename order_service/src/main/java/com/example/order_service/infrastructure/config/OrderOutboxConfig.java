package com.example.order_service.infrastructure.config;

import com.example.order_service.cart.application.port.in.ProcessOrderOutboxUseCase;
import com.example.order_service.cart.application.port.out.kafka.OrderEventPublisherPort;
import com.example.order_service.cart.application.port.out.outbox.OutboxPort;
import com.example.order_service.cart.application.registry.OutboxEventHandlerRegistry;
import com.example.order_service.cart.application.service.OrderOutboxStatusService;
import com.example.order_service.cart.application.strategy.outbox.CartItemAddedOutboxHandler;
import com.example.order_service.cart.application.strategy.outbox.OutboxEventHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.List;

@Configuration
@EnableScheduling
public class OrderOutboxConfig {
    @Bean
    ProcessOrderOutboxUseCase processOrderOutboxUseCase(OutboxPort outboxPort,
                                                       OutboxEventHandlerRegistry registry) {
        return new OrderOutboxStatusService(outboxPort, registry);
    }

    @Bean
    OutboxEventHandler cartItemAddedOutboxHandler(OrderEventPublisherPort publisher) {
        return new CartItemAddedOutboxHandler(publisher);
    }

    @Bean
    OutboxEventHandlerRegistry outboxEventHandlerRegistry(List<OutboxEventHandler> handlers) {
        return new OutboxEventHandlerRegistry(handlers);
    }
}
