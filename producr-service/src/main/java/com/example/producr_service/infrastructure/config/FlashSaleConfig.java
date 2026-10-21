package com.example.producr_service.infrastructure.config;

import com.example.producr_service.application.port.in.CreateFlashSaleCampaignUseCase;
import com.example.producr_service.application.port.in.GetFlashSaleCampaignsUseCase;
import com.example.producr_service.application.port.in.GetUserFlashSaleItemsUseCase;
import com.example.producr_service.application.port.in.GetShopFlashSaleItemsUseCase;
import com.example.producr_service.application.port.out.repo.FlashSaleCampaignRepositoryPort;
import com.example.producr_service.application.service.FlashSaleCampaignService;
import com.example.producr_service.application.port.in.CancelFlashSaleCampaignUseCase;
import com.example.producr_service.application.port.in.GetShopFlashSaleCampaignsUseCase;
import com.example.producr_service.application.port.in.GetShopFlashSaleCampaignDetailUseCase;
import com.example.producr_service.application.port.in.GetShopFlashSaleProductsUseCase;
import com.example.producr_service.application.port.out.client.CurrentUserPort;
import com.example.producr_service.application.service.ProductService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.example.producr_service.application.service.FlashSaleItemService;
import com.example.producr_service.application.service.VariantSaleResolver;
import com.example.producr_service.application.port.in.RegisterFlashSaleItemUseCase;
import com.example.producr_service.application.port.in.RemoveShopFlashSaleItemUseCase;
import com.example.producr_service.application.port.in.UpdateShopFlashSaleItemUseCase;
import com.example.producr_service.application.port.in.PreviewFlashSalePriceUseCase;
import com.example.producr_service.application.port.in.GetVariantSaleUseCase;
import com.example.producr_service.application.port.out.repo.ProductVariantRepoPort;
import com.example.producr_service.application.port.out.repo.FlashSaleItemRepositoryPort;
import java.time.Clock;

import com.example.producr_service.application.port.in.ProcessFlashSaleCampaignStatusUseCase;
import com.example.producr_service.application.service.ProcessFlashSaleCampaignStatuService;
import com.example.producr_service.application.registry.FlashSaleCampaignStatuRegistry;
import com.example.producr_service.application.strategy.FlashSaleCampaign.FlashSaleCampaignStatuHandler;
import com.example.producr_service.application.strategy.FlashSaleCampaign.FlashSaleCampaignDraftHandler;
import com.example.producr_service.application.strategy.FlashSaleCampaign.FlashSaleCampaignPublishedHandler;

import java.util.List;

@Configuration
public class FlashSaleConfig {

    @Bean
    GetShopFlashSaleItemsUseCase getShopFlashSaleItemsUseCase(FlashSaleItemService flashSaleItemService) {
        return flashSaleItemService;
    }

    @Bean
    VariantSaleResolver variantSaleResolver(Clock clock) {
        return new VariantSaleResolver(clock);
    }

    @Bean
    FlashSaleItemService flashSaleItemService(CurrentUserPort currentUser,
            FlashSaleCampaignRepositoryPort campaigns, FlashSaleItemRepositoryPort items, Clock clock,
            ProductVariantRepoPort productVariantRepoPort, VariantSaleResolver variantSaleResolver) {
        return new FlashSaleItemService(currentUser, campaigns, items, clock, productVariantRepoPort,
                variantSaleResolver);
    }

    @Bean
    GetVariantSaleUseCase getVariantSaleUseCase(FlashSaleItemService flashSaleItemService) {
        return flashSaleItemService;
    }

    @Bean
    RegisterFlashSaleItemUseCase registerFlashSaleItemUseCase(
            FlashSaleItemService flashSaleItemService) {
        return flashSaleItemService;
    }

    @Bean
    RemoveShopFlashSaleItemUseCase removeShopFlashSaleItemUseCase(FlashSaleItemService flashSaleItemService) {
        return flashSaleItemService;
    }

    @Bean
    UpdateShopFlashSaleItemUseCase updateShopFlashSaleItemUseCase(FlashSaleItemService flashSaleItemService) {
        return flashSaleItemService;
    }

    @Bean
    PreviewFlashSalePriceUseCase previewFlashSalePriceUseCase(
            FlashSaleItemService flashSaleItemService) {
        return flashSaleItemService;
    }

    @Bean
    GetUserFlashSaleItemsUseCase getUserFlashSaleItemsUseCase(FlashSaleItemService flashSaleItemService) {
        return flashSaleItemService;
    }

    @Bean
    FlashSaleCampaignService flashSaleCampaignService(
            CurrentUserPort currentUserPort,
            FlashSaleCampaignRepositoryPort flashSaleCampaignRepositoryPort,
            Clock clock
    ) {
        return new FlashSaleCampaignService(currentUserPort, flashSaleCampaignRepositoryPort, clock);
    }

    @Bean
    CreateFlashSaleCampaignUseCase createFlashSaleCampaignUseCase(FlashSaleCampaignService flashSaleCampaignService) {
        return flashSaleCampaignService;
    }

    @Bean
    GetFlashSaleCampaignsUseCase getFlashSaleCampaignsUseCase(FlashSaleCampaignService flashSaleCampaignService) {
        return flashSaleCampaignService;
    }

    @Bean
    GetShopFlashSaleCampaignsUseCase getShopFlashSaleCampaignsUseCase(
            FlashSaleCampaignService flashSaleCampaignService) {
        return flashSaleCampaignService;
    }

    @Bean
    GetShopFlashSaleCampaignDetailUseCase getShopFlashSaleCampaignDetailUseCase(
            FlashSaleCampaignService flashSaleCampaignService) {
        return flashSaleCampaignService;
    }

    @Bean
    CancelFlashSaleCampaignUseCase cancelFlashSaleCampaignUseCase(
            FlashSaleCampaignService flashSaleCampaignService) {
        return flashSaleCampaignService;
    }

    @Bean
    GetShopFlashSaleProductsUseCase getShopFlashSaleProductsUseCase(ProductService productService) {
        return productService;
    }

    @Bean
    FlashSaleCampaignStatuHandler flashSaleCampaignDraftHandler(Clock clock) {
        return new FlashSaleCampaignDraftHandler(clock);
    }

    @Bean
    FlashSaleCampaignStatuHandler flashSaleCampaignPublishedHandler(Clock clock) {
        return new FlashSaleCampaignPublishedHandler(clock);
    }

    @Bean
    FlashSaleCampaignStatuRegistry flashSaleCampaignStatuRegistry(
            List<FlashSaleCampaignStatuHandler> handlers) {
        return new FlashSaleCampaignStatuRegistry(handlers);
    }

    @Bean
    ProcessFlashSaleCampaignStatusUseCase processFlashSaleCampaignStatusUseCase(
            FlashSaleCampaignRepositoryPort campaigns, FlashSaleCampaignStatuRegistry registry, Clock clock) {
        return new ProcessFlashSaleCampaignStatuService(campaigns, registry, clock);
    }
}
