package com.example.producr_service.infrastructure.config;

import com.example.producr_service.application.port.in.ProcessOrderEventUseCase;
import com.example.producr_service.application.port.out.Json.MapJsonToObjPort;
import com.example.producr_service.application.registry.KafkaEventHandlerRegistry;
import com.example.producr_service.application.service.OrderEventApplicationService;
import com.example.producr_service.application.service.ProductRecommendationService;
import com.example.producr_service.application.strategy.eventKafka.CartItemAddedEventHandler;
import com.example.producr_service.application.strategy.eventKafka.EventHandler;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OrderEventConfig {
    @Bean
    CartItemAddedEventHandler cartItemAddedEventHandler(
            MapJsonToObjPort mapJsonToObjPort, ProductRecommendationService productRecommendationService) {
        return new CartItemAddedEventHandler(mapJsonToObjPort, productRecommendationService);
    }

    @Bean
    KafkaEventHandlerRegistry kafkaEventHandlerRegistry(List<EventHandler> eventHandlers) {
        return new KafkaEventHandlerRegistry(eventHandlers);
    }

    @Bean
    ProcessOrderEventUseCase processOrderEventUseCase(KafkaEventHandlerRegistry registry) {
        return new OrderEventApplicationService(registry);
    }
}
