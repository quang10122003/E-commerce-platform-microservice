package com.example.producr_service.infrastructure.config;

import com.example.producr_service.application.port.in.ProcessAuthEventUseCase;
import com.example.producr_service.application.service.AuthEventApplicationService;
import com.example.producr_service.application.service.ProductRecommendationService;
import com.example.producr_service.application.strategy.eventKafka.UserDeletedEventHandler;
import com.example.producr_service.application.service.ShopAccessService;

import com.example.producr_service.application.strategy.EventHandler;
import com.example.producr_service.application.port.in.ProcessOrderEventUseCase;
import com.example.producr_service.application.port.out.Json.MapJsonToObjPort;
import com.example.producr_service.application.registry.KafkaEventHandlerRegistry;
import com.example.producr_service.application.service.OrderEventApplicationService;
import com.example.producr_service.application.service.ProductRecommendationService;
import com.example.producr_service.application.strategy.eventKafka.CartItemAddedEventHandler;
import com.example.producr_service.application.strategy.eventKafka.CheckoutInventoryRequestedHandler;
import com.example.producr_service.application.strategy.eventKafka.CheckoutSucceededHandler;
import com.example.producr_service.application.strategy.eventKafka.CheckoutInventoryRollbackRequestedHandler;
import com.example.producr_service.application.strategy.eventKafka.OrderInventoryRollbackRequestedHandler;
import com.example.producr_service.application.service.CheckoutInventoryService;
import com.example.producr_service.application.port.out.repo.FlashSaleCampaignRepositoryPort;
import com.example.producr_service.application.port.out.repo.FlashSaleItemRepositoryPort;
import com.example.producr_service.application.port.out.repo.InboxEventPort;
import com.example.producr_service.application.port.out.repo.ProductBehaviorRepositoryPort;
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
    EventHandler<String> checkoutSucceededHandler(MapJsonToObjPort json, CheckoutInventoryService service) {
        return new CheckoutSucceededHandler(json, service);
    }

    @Bean
    CheckoutInventoryService checkoutInventoryService(InboxEventPort inbox,
            ProductRepositoryPort productRepositoryPort, ProductVariantRepoPort productVariantRepoPort,
            FlashSaleCampaignRepositoryPort flashSaleCampaignRepositoryPort,
            FlashSaleItemRepositoryPort flashSaleItemRepositoryPort,
            OutboxPort outbox, MapJsonToObjPort json, Clock clock,
            ShopAccessService shopAccessService, ProductBehaviorRepositoryPort productBehaviorRepositoryPort) {
        return new CheckoutInventoryService(inbox, productRepositoryPort, productVariantRepoPort,
                flashSaleCampaignRepositoryPort, flashSaleItemRepositoryPort, outbox, json, clock, shopAccessService,
                productBehaviorRepositoryPort);
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
    EventHandler<String> orderInventoryRollbackRequestedHandler(MapJsonToObjPort json, CheckoutInventoryService service) {
        return new OrderInventoryRollbackRequestedHandler(json, service);
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

    @Bean
    ProcessAuthEventUseCase processAuthEventUseCase(KafkaEventHandlerRegistry registry) {
        return new AuthEventApplicationService(registry);
    }

    @Bean
    UserDeletedEventHandler userDeletedEventHandler(MapJsonToObjPort mapJsonToObjPort,
                                                    ProductRecommendationService productRecommendationService) {
        return new UserDeletedEventHandler(mapJsonToObjPort, productRecommendationService);
    }
}
