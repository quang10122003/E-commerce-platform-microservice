package com.example.producr_service.infrastructure.config;

import com.example.producr_service.application.port.in.CreateFlashSaleCampaignUseCase;
import com.example.producr_service.application.port.in.GetFlashSaleCampaignsUseCase;
import com.example.producr_service.application.port.out.repo.FlashSaleCampaignRepositoryPort;
import com.example.producr_service.application.service.FlashSaleCampaignService;
import org.springframework.beans.factory.annotation.Qualifier;
import com.example.producr_service.application.port.in.CancelFlashSaleCampaignUseCase;
import com.example.producr_service.application.port.in.GetShopFlashSaleCampaignsUseCase;
import com.example.producr_service.application.port.in.GetShopFlashSaleCampaignDetailUseCase;
import com.example.producr_service.application.port.in.GetShopFlashSaleProductsUseCase;
import com.example.producr_service.application.port.out.client.CurrentUserPort;
import com.example.producr_service.application.service.ProductService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.example.producr_service.application.service.FlashSaleItemService;
import com.example.producr_service.application.port.in.RegisterFlashSaleItemUseCase;
import com.example.producr_service.application.port.in.PreviewFlashSalePriceUseCase;
import com.example.producr_service.application.port.out.repo.FlashSaleItemRepositoryPort;
import java.time.Clock;



@Configuration
public class FlashSaleConfig {

    // Lắp ghép các port cho đăng ký variant và tính giá sale dự kiến.
    @Bean
    FlashSaleItemService flashSaleItemService(CurrentUserPort currentUser,
            FlashSaleCampaignRepositoryPort campaigns, FlashSaleItemRepositoryPort items, Clock clock) {
        return new FlashSaleItemService(currentUser, campaigns, items, clock);
    }

    // Cung cấp input port đăng ký variant cho controller.
    @Bean
    RegisterFlashSaleItemUseCase registerFlashSaleItemUseCase(
            @Qualifier("flashSaleItemService") FlashSaleItemService service) {
        return service;
    }

    // Cung cấp input port tính giá dự kiến cho controller.
    @Bean
    PreviewFlashSalePriceUseCase previewFlashSalePriceUseCase(
            @Qualifier("flashSaleItemService") FlashSaleItemService service) {
        return service;
    }

    // Khởi tạo service dùng chung cho các use case chương trình Flash Sale.
    @Bean
    FlashSaleCampaignService flashSaleCampaignService(
            CurrentUserPort currentUserPort,
            FlashSaleCampaignRepositoryPort flashSaleCampaignRepositoryPort,
            Clock clock
    ) {
        return new FlashSaleCampaignService(currentUserPort, flashSaleCampaignRepositoryPort, clock);
    }

    // Cung cấp use case tạo chương trình Flash Sale qua input port.
    @Bean
    CreateFlashSaleCampaignUseCase createFlashSaleCampaignUseCase(@Qualifier("flashSaleCampaignService") FlashSaleCampaignService service) {
        return service;
    }

    // Cung cấp use case đọc danh sách chương trình cho controller.
    @Bean
    GetFlashSaleCampaignsUseCase getFlashSaleCampaignsUseCase(@Qualifier("flashSaleCampaignService") FlashSaleCampaignService service) {
        return service;
    }

    // Cung cấp use case đọc chương trình cho Shop từ service Flash Sale dùng chung.
    @Bean
    GetShopFlashSaleCampaignsUseCase getShopFlashSaleCampaignsUseCase(
            @Qualifier("flashSaleCampaignService") FlashSaleCampaignService service) {
        return service;
    }

    // Lắp ghép use case đọc chi tiết cho biểu mẫu đăng ký của Shop.
    @Bean
    GetShopFlashSaleCampaignDetailUseCase getShopFlashSaleCampaignDetailUseCase(
            @Qualifier("flashSaleCampaignService") FlashSaleCampaignService service) {
        return service;
    }

    // Cung cấp use case hủy chương trình qua input port.
    @Bean
    CancelFlashSaleCampaignUseCase cancelFlashSaleCampaignUseCase(
            @Qualifier("flashSaleCampaignService") FlashSaleCampaignService service) {
        return service;
    }

    // Lắp ghép input port chọn sản phẩm Flash Sale với service sản phẩm hiện có.
    @Bean
    GetShopFlashSaleProductsUseCase getShopFlashSaleProductsUseCase(ProductService productService) {
        return productService;
    }

}
