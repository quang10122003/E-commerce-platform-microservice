package com.example.producr_service.infrastructure.config;

import com.example.producr_service.application.port.in.CreateFlashSaleCampaignUseCase;
import com.example.producr_service.application.port.in.GetFlashSaleCampaignsUseCase;
import com.example.producr_service.application.port.in.GetUserFlashSaleItemsUseCase;
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
import com.example.producr_service.application.port.in.RegisterFlashSaleItemUseCase;
import com.example.producr_service.application.port.in.PreviewFlashSalePriceUseCase;
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

    // Lắp ghép các port cho đăng ký variant và tính giá sale dự kiến.
    @Bean
    FlashSaleItemService flashSaleItemService(CurrentUserPort currentUser,
            FlashSaleCampaignRepositoryPort campaigns, FlashSaleItemRepositoryPort items, Clock clock) {
        return new FlashSaleItemService(currentUser, campaigns, items, clock);
    }

    // Cung cấp input port đăng ký variant cho controller.
    @Bean
    RegisterFlashSaleItemUseCase registerFlashSaleItemUseCase(
            FlashSaleItemService flashSaleItemService) {
        return flashSaleItemService;
    }

    // Cung cấp input port tính giá dự kiến cho controller.
    @Bean
    PreviewFlashSalePriceUseCase previewFlashSalePriceUseCase(
            FlashSaleItemService flashSaleItemService) {
        return flashSaleItemService;
    }

    // Cung cấp input port lấy card sale theo phân loại cho controller phía user.
    @Bean
    GetUserFlashSaleItemsUseCase getUserFlashSaleItemsUseCase(FlashSaleItemService flashSaleItemService) {
        return flashSaleItemService;
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
    CreateFlashSaleCampaignUseCase createFlashSaleCampaignUseCase(FlashSaleCampaignService flashSaleCampaignService) {
        return flashSaleCampaignService;
    }

    // Cung cấp use case đọc danh sách chương trình cho controller.
    @Bean
    GetFlashSaleCampaignsUseCase getFlashSaleCampaignsUseCase(FlashSaleCampaignService flashSaleCampaignService) {
        return flashSaleCampaignService;
    }

    // Cung cấp use case đọc chương trình cho Shop từ service Flash Sale dùng chung.
    @Bean
    GetShopFlashSaleCampaignsUseCase getShopFlashSaleCampaignsUseCase(
            FlashSaleCampaignService flashSaleCampaignService) {
        return flashSaleCampaignService;
    }

    // Lắp ghép use case đọc chi tiết cho biểu mẫu đăng ký của Shop.
    @Bean
    GetShopFlashSaleCampaignDetailUseCase getShopFlashSaleCampaignDetailUseCase(
            FlashSaleCampaignService flashSaleCampaignService) {
        return flashSaleCampaignService;
    }

    // Cung cấp use case hủy chương trình qua input port.
    @Bean
    CancelFlashSaleCampaignUseCase cancelFlashSaleCampaignUseCase(
            FlashSaleCampaignService flashSaleCampaignService) {
        return flashSaleCampaignService;
    }

    // Lắp ghép input port chọn sản phẩm Flash Sale với service sản phẩm hiện có.
    @Bean
    GetShopFlashSaleProductsUseCase getShopFlashSaleProductsUseCase(ProductService productService) {
        return productService;
    }

    // Đăng ký handler DRAFT qua interface của application.
    @Bean
    FlashSaleCampaignStatuHandler flashSaleCampaignDraftHandler(Clock clock) {
        return new FlashSaleCampaignDraftHandler(clock);
    }

    // Đăng ký handler PUBLISHED qua interface của application.
    @Bean
    FlashSaleCampaignStatuHandler flashSaleCampaignPublishedHandler(Clock clock) {
        return new FlashSaleCampaignPublishedHandler(clock);
    }

    // Gom các handler và phát hiện trạng thái bị đăng ký trùng.
    @Bean
    FlashSaleCampaignStatuRegistry flashSaleCampaignStatuRegistry(
            List<FlashSaleCampaignStatuHandler> handlers) {
        return new FlashSaleCampaignStatuRegistry(handlers);
    }

    // Lắp ghép input port xử lý trạng thái cho scheduler.
    @Bean
    ProcessFlashSaleCampaignStatusUseCase processFlashSaleCampaignStatusUseCase(
            FlashSaleCampaignRepositoryPort campaigns, FlashSaleCampaignStatuRegistry registry, Clock clock) {
        return new ProcessFlashSaleCampaignStatuService(campaigns, registry, clock);
    }
}
