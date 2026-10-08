package com.example.producr_service.application.service;

import com.example.common.exception.BusinessException;
import com.example.producr_service.application.dto.request.*;
import com.example.producr_service.application.dto.response.*;
import com.example.producr_service.application.error.ProductError;
import com.example.producr_service.application.port.in.*;
import com.example.producr_service.application.port.out.client.CurrentUserPort;
import com.example.producr_service.application.port.out.repo.*;
import com.example.producr_service.domain.service.FlashSaleRules;
import java.time.Clock;
import java.time.LocalDateTime;
import org.springframework.transaction.annotation.Transactional;

public class FlashSaleItemService implements RegisterFlashSaleItemUseCase, PreviewFlashSalePriceUseCase {
    private final CurrentUserPort currentUser;
    private final FlashSaleCampaignRepositoryPort campaigns;
    private final FlashSaleItemRepositoryPort items;
    private final Clock clock;

    public FlashSaleItemService(CurrentUserPort currentUser, FlashSaleCampaignRepositoryPort campaigns,
                                FlashSaleItemRepositoryPort items, Clock clock) {
        this.currentUser = currentUser;
        this.campaigns = campaigns;
        this.items = items;
        this.clock = clock;
    }

    // Khóa campaign và tải đầy đủ items trước khi domain thêm đăng ký vào aggregate.
    @Override
    @Transactional
    public void register(Long campaignId, RegisterFlashSaleItemRequest request) {
        Long shopId = currentUser.getUserInfo().userId();
        // lấy chương trình sale
        var campaign = campaigns.findByIdForUpdate(campaignId)
                .orElseThrow(() -> new BusinessException(ProductError.FLASH_SALE_CAMPAIGN_NOT_FOUND));

        var variant = ownedVariant(request.variantId(), shopId);
        LocalDateTime now = LocalDateTime.now(clock);
        campaign.addItem(variant.toRegistrationVariant(),
                request.discountPercent(), request.registeredQuantity(), now);
        campaigns.save(campaign);
    }

    // Tính giá dự kiến từ giá hiện tại của variant thuộc Shop, không ghi dữ liệu.
    @Override
    @Transactional(readOnly = true)
    public FlashSalePriceResponse previewPrice(Long variantId, PreviewFlashSalePriceRequest request) {
        var variant = ownedVariant(variantId, currentUser.getUserInfo().userId());
        return new FlashSalePriceResponse(
                FlashSaleRules.calculateSalePrice(variant.price(), request.discountPercent()));
    }

    // check quyền sở hữu variant và sản phầm phải active
    private FlashSaleVariantSnapshot ownedVariant(Long variantId, Long shopId) {
        var variant = items.findVariant(variantId)
                .orElseThrow(() -> new BusinessException(ProductError.PRODUCT_VARIANT_NOT_FOUND));
        if (!variant.shopId().equals(shopId)) {
            throw new BusinessException(ProductError.PRODUCT_ACCESS_DENIED);
        }
        if (!variant.active()) {
            throw new BusinessException(ProductError.FLASH_SALE_PRODUCT_INACTIVE);
        }
        return variant;
    }
}
