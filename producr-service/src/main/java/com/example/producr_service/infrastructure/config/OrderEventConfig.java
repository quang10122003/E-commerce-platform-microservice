package com.example.producr_service.infrastructure.config;

import com.example.producr_service.application.strategy.EventHandler;
import com.example.producr_service.application.port.in.ProcessOrderEventUseCase;
import com.example.producr_service.application.port.out.Json.MapJsonToObjPort;
import com.example.producr_service.application.registry.KafkaEventHandlerRegistry;
import com.example.producr_service.application.service.OrderEventApplicationService;
import com.example.producr_service.application.service.ProductRecommendationService;
import com.example.producr_service.application.strategy.eventKafka.CartItemAddedEventHandler;
import com.example.producr_service.application.strategy.eventKafka.CheckoutInventoryRequestedHandler;
import com.example.producr_service.application.strategy.eventKafka.CheckoutInventoryRollbackRequestedHandler;
import com.example.producr_service.application.service.CheckoutInventoryService;
import com.example.producr_service.application.port.out.repo.FlashSaleCampaignRepositoryPort;
import com.example.producr_service.application.port.out.repo.FlashSaleItemRepositoryPort;
import com.example.producr_service.application.port.out.repo.InboxEventPort;
import com.example.producr_service.application.port.out.repo.ProductRepositoryPort;
import com.example.producr_service.application.port.out.repo.ProductVariantRepoPort;
import com.example.producr_service.application.port.out.outbox.OutboxPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.util.List;

@Configuration
public class OrderEventConfig {
    @Bean
    CheckoutInventoryService checkoutInventoryService(InboxEventPort inbox,
            ProductRepositoryPort productRepositoryPort, ProductVariantRepoPort productVariantRepoPort,
            FlashSaleCampaignRepositoryPort flashSaleCampaignRepositoryPort,
            FlashSaleItemRepositoryPort flashSaleItemRepositoryPort,
            OutboxPort outbox, MapJsonToObjPort json, Clock clock) {
        return new CheckoutInventoryService(inbox, productRepositoryPort, productVariantRepoPort,
                flashSaleCampaignRepositoryPort, flashSaleItemRepositoryPort, outbox, json, clock);
    }

    @Bean
    EventHandler<String> checkoutInventoryRequestedHandler(MapJsonToObjPort json, CheckoutInventoryService service) {
        return new CheckoutInventoryRequestedHandler(json, service);
    }

    @Bean
    EventHandler<String> checkoutInventoryRollbackRequestedHandler(MapJsonToObjPort json, CheckoutInventoryService service) {
        return new CheckoutInventoryRollbackRequestedHandler(json, service);
    }

    @Bean
    CartItemAddedEventHandler cartItemAddedEventHandler(
            MapJsonToObjPort mapJsonToObjPort, ProductRecommendationService productRecommendationService) {
        return new CartItemAddedEventHandler(mapJsonToObjPort, productRecommendationService);
    }

    @Bean
    KafkaEventHandlerRegistry kafkaEventHandlerRegistry(List<EventHandler<String>> eventHandlers) {
        return new KafkaEventHandlerRegistry(eventHandlers);
    }

    @Bean
    ProcessOrderEventUseCase processOrderEventUseCase(KafkaEventHandlerRegistry registry) {
        return new OrderEventApplicationService(registry);
    }
}
