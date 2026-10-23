package com.example.producr_service.infrastructure.config;

import com.example.producr_service.application.port.in.RecordProductViewUseCase;
import com.example.producr_service.application.port.out.client.CurrentUserPort;
import com.example.producr_service.application.port.out.repo.ProductBehaviorRepositoryPort;
import com.example.producr_service.application.port.out.repo.ProductRepositoryPort;
import com.example.producr_service.application.port.out.repo.ProductViewStateRepositoryPort;
import com.example.producr_service.application.port.out.repo.ProductVariantRepoPort;
import com.example.producr_service.application.service.ProductRecommendationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class ProductRecommendationConfig {
    @Bean
    ProductRecommendationService productRecommendationService(
            CurrentUserPort currentUserPort,
            ProductRepositoryPort productRepositoryPort,
            ProductBehaviorRepositoryPort productBehaviorRepositoryPort,
            ProductViewStateRepositoryPort productViewStateRepositoryPort,
            Clock clock, ProductVariantRepoPort productVariantRepoPort) {
        return new ProductRecommendationService(currentUserPort, productRepositoryPort,
                productBehaviorRepositoryPort, productViewStateRepositoryPort, clock, productVariantRepoPort);
    }

    @Bean
    RecordProductViewUseCase recordProductViewUseCase(ProductRecommendationService productRecommendationService) {
        return productRecommendationService;
    }
}
