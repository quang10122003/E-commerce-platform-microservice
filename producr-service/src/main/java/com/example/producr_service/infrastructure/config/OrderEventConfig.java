package com.example.producr_service.infrastructure.config;

import com.example.producr_service.application.service.KafkaEventApplicationService;
import com.example.producr_service.application.service.ProductRecommendationService;
import com.example.producr_service.application.strategy.eventKafka.UserDeletedEventHandler;
import com.example.producr_service.application.strategy.eventKafka.ShopLockChangedEventHandler;
import com.example.producr_service.application.service.ProductShopLockService;
import com.example.producr_service.application.port.out.cache.CachePort;
import com.example.producr_service.application.port.out.cache.FlashSaleSlotPort;

import com.example.producr_service.application.strategy.EventHandler;
import com.example.producr_service.application.port.out.Json.MapJsonToObjPort;
import com.example.producr_service.application.registry.KafkaEventHandlerRegistry;
import com.example.producr_service.application.service.ProductRecommendationService;
import com.example.producr_service.application.strategy.eventKafka.CartItemAddedEventHandler;
import com.example.producr_service.application.strategy.eventKafka.CheckoutInventoryRequestedHandler;
import com.example.producr_service.application.strategy.eventKafka.CheckoutSucceededHandler;
import com.example.producr_service.application.strategy.eventKafka.ProductReviewedHandler;
import com.example.producr_service.application.service.ProductReviewEventService;
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
    ProductReviewEventService productReviewEventService(InboxEventPort inbox, ProductRepositoryPort productRepositoryPort,
                                                        OutboxPort outbox, MapJsonToObjPort json, Clock clock) {
        return new ProductReviewEventService(inbox, productRepositoryPort, outbox, json, clock);
    }

    @Bean
    EventHandler<String> productReviewedHandler(MapJsonToObjPort json, ProductReviewEventService service) {
        return new ProductReviewedHandler(json, service);
    }

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
            ProductBehaviorRepositoryPort productBehaviorRepositoryPort, FlashSaleSlotPort flashSaleSlotPort) {
        return new CheckoutInventoryService(inbox, productRepositoryPort, productVariantRepoPort,
                flashSaleCampaignRepositoryPort, flashSaleItemRepositoryPort, outbox, json, clock,
                productBehaviorRepositoryPort, flashSaleSlotPort);
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

    @Bean(name = {"kafkaEventApplicationService", "processOrderEventUseCase", "processAuthEventUseCase"})
    KafkaEventApplicationService kafkaEventApplicationService(KafkaEventHandlerRegistry registry) {
        return new KafkaEventApplicationService(registry);
    }

    @Bean
    ProductShopLockService productShopLockService(ProductRepositoryPort productRepositoryPort, OutboxPort outbox,
                                                  CachePort cachePort, Clock clock) {
        return new ProductShopLockService(productRepositoryPort, outbox, cachePort, clock);
    }

    @Bean
    ShopLockChangedEventHandler shopLockChangedEventHandler(MapJsonToObjPort mapJsonToObjPort,
                                                            ProductShopLockService productShopLockService) {
        return new ShopLockChangedEventHandler(mapJsonToObjPort, productShopLockService);
    }

    @Bean
    UserDeletedEventHandler userDeletedEventHandler(MapJsonToObjPort mapJsonToObjPort,
                                                    ProductRecommendationService productRecommendationService) {
        return new UserDeletedEventHandler(mapJsonToObjPort, productRecommendationService);
    }
}
