package com.example.producr_service.infrastructure.config;

import com.example.producr_service.application.port.in.RecordProductViewUseCase;
import com.example.producr_service.application.port.in.GetRecommendedProductsUseCase;
import com.example.producr_service.application.port.in.GetSimilarProductsUseCase;
import com.example.producr_service.application.port.out.cache.CachePort;
import com.example.producr_service.application.port.out.embedding.ProductEmbeddingRepositoryPort;
import com.example.producr_service.application.port.out.client.CurrentUserPort;
import com.example.producr_service.application.port.out.repo.ProductBehaviorRepositoryPort;
import com.example.producr_service.application.port.out.repo.ProductRepositoryPort;
import com.example.producr_service.application.port.out.repo.ProductViewStateRepositoryPort;
import com.example.producr_service.application.port.out.repo.ProductVariantRepoPort;
import com.example.producr_service.application.service.ProductRecommendationService;
import com.example.producr_service.application.service.UserPreferenceCalculator;
import com.example.producr_service.application.service.ProductService;
import com.example.producr_service.application.port.in.CleanupExpiredProductBehaviorsUseCase;
import com.example.producr_service.application.service.ProductBehaviorCleanupBatchService;
import com.example.producr_service.application.service.ProductBehaviorCleanupService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class ProductRecommendationConfig {
    @Bean
    GetSimilarProductsUseCase getSimilarProductsUseCase(
            ProductRecommendationService productRecommendationService) {
        return productRecommendationService;
    }

    @Bean
    UserPreferenceCalculator userPreferenceCalculator() {
        return new UserPreferenceCalculator();
    }

    @Bean
    GetRecommendedProductsUseCase getRecommendedProductsUseCase(
            ProductRecommendationService productRecommendationService) {
        return productRecommendationService;
    }

    @Bean
    ProductBehaviorCleanupBatchService productBehaviorCleanupBatchService(ProductBehaviorRepositoryPort repository) {
        return new ProductBehaviorCleanupBatchService(repository);
    }

    @Bean
    CleanupExpiredProductBehaviorsUseCase cleanupExpiredProductBehaviorsUseCase(
            ProductBehaviorCleanupBatchService batchService, Clock clock,
            @Value("${app.recommendation.behavior-cleanup.batch-size}") int batchSize) {
        return new ProductBehaviorCleanupService(batchService, clock, batchSize);
    }

    @Bean
    ProductRecommendationService productRecommendationService(
            CurrentUserPort currentUserPort,
            ProductRepositoryPort productRepositoryPort,
            ProductBehaviorRepositoryPort productBehaviorRepositoryPort,
            ProductViewStateRepositoryPort productViewStateRepositoryPort,
            Clock clock, ProductVariantRepoPort productVariantRepoPort,
            ProductEmbeddingRepositoryPort productEmbeddingRepositoryPort,
            CachePort cachePort,
            UserPreferenceCalculator userPreferenceCalculator, ProductService productService) {
        return new ProductRecommendationService(currentUserPort, productRepositoryPort,
                productBehaviorRepositoryPort, productViewStateRepositoryPort, clock, productVariantRepoPort,
                productEmbeddingRepositoryPort, cachePort,
                userPreferenceCalculator, productService);
    }

    @Bean
    RecordProductViewUseCase recordProductViewUseCase(ProductRecommendationService productRecommendationService) {
        return productRecommendationService;
    }
}
