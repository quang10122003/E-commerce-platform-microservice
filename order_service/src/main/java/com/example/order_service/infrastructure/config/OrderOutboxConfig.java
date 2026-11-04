package com.example.order_service.infrastructure.config;

import com.example.order_service.application.dto.outbox.OutboxEventDto;
import com.example.order_service.application.strategy.EventHandler;
import com.example.order_service.application.port.in.ProcessOrderOutboxUseCase;
import com.example.order_service.application.port.out.kafka.OrderEventPublisherPort;
import com.example.order_service.application.port.out.outbox.OutboxPort;
import com.example.order_service.application.registry.OutboxEventHandlerRegistry;
import com.example.order_service.application.service.OrderOutboxStatusService;
import com.example.order_service.application.strategy.outbox.CartItemAddedOutboxHandler;
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
    EventHandler<OutboxEventDto> cartItemAddedOutboxHandler(OrderEventPublisherPort publisher) {
        return new CartItemAddedOutboxHandler(publisher);
    }

    @Bean
    OutboxEventHandlerRegistry outboxEventHandlerRegistry(List<EventHandler<OutboxEventDto>> handlers) {
        return new OutboxEventHandlerRegistry(handlers);
    }
}
