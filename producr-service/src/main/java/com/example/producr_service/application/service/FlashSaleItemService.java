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
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class FlashSaleItemService implements RegisterFlashSaleItemUseCase, PreviewFlashSalePriceUseCase,GetUserFlashSaleItemsUseCase {
     CurrentUserPort currentUser;
     FlashSaleCampaignRepositoryPort flashSaleCampaignRepositoryPort;
     FlashSaleItemRepositoryPort flashSaleItemRepositoryPort;
     Clock clock;

    // Khóa campaign và tải đầy đủ items trước khi domain thêm đăng ký vào aggregate.
    @Override
    @Transactional
    public void register(Long campaignId, RegisterFlashSaleItemRequest request) {
        Long shopId = currentUser.getUserInfo().userId();
        // lấy chương trình sale
        var campaign = flashSaleCampaignRepositoryPort.findByIdForUpdate(campaignId)
                .orElseThrow(() -> new BusinessException(ProductError.FLASH_SALE_CAMPAIGN_NOT_FOUND));

        var variant = ownedVariant(request.variantId(), shopId);
        LocalDateTime now = LocalDateTime.now(clock);
        campaign.addItem(variant.toRegistrationVariant(),
                request.discountPercent(), request.registeredQuantity(), now);
        flashSaleCampaignRepositoryPort.save(campaign);
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
        var variant = flashSaleItemRepositoryPort.findVariant(variantId)
                .orElseThrow(() -> new BusinessException(ProductError.PRODUCT_VARIANT_NOT_FOUND));
        if (!variant.shopId().equals(shopId)) {
            throw new BusinessException(ProductError.PRODUCT_ACCESS_DENIED);
        }
        if (!variant.active()) {
            throw new BusinessException(ProductError.FLASH_SALE_PRODUCT_INACTIVE);
        }
        return variant;
    }

    // lấy thông tin item của sale cho user
    @Transactional(readOnly = true)
    @Override
    public UserFlashSaleResponse getItems(Long categoryId) {
        LocalDateTime now = LocalDateTime.now(clock);
        List<UserFlashSaleItemView> items = flashSaleItemRepositoryPort.findItemsInOngoingCampaigns(categoryId, now);
        if (items.isEmpty()) {
            return new UserFlashSaleResponse(null, List.of());
        }

        // Lịch sale không chồng nhau nên các item có chung thời gian hết hạn.
        return new UserFlashSaleResponse(
                items.getFirst().endsAt().atZone(clock.getZone()).toOffsetDateTime(),
                items.stream().map(this::toUserItemResponse).toList()
        );
    }


//    dựng repone UserFlashSaleItemResponse cho api lấy thông tin sản phẩm của sale đang phát hành
    private UserFlashSaleItemResponse toUserItemResponse(UserFlashSaleItemView item) {
        VariantCartView variant = item.variant();

        // Số lượng mua được bị giới hạn bởi cả quota sale và tồn kho thực tế.
        long quotaRemaining = Math.max(
                0L,
                (long) item.registeredQuantity() - item.soldQuantity()
        );
        long stockAvailable = Math.max(0L, variant.stockQuantity());

        long remainingQuantity = Math.min(quotaRemaining, stockAvailable);

        // Hết quota được ưu tiên vì nhập thêm hàng cũng không khôi phục suất sale.
        UserFlashSaleItemResponse.Availability availability;
        if (quotaRemaining == 0) {  // hết lượt mua sale
            availability = UserFlashSaleItemResponse.Availability.SOLD_OUT;
        } else if (stockAvailable == 0) { // tạm hết hàng
            availability = UserFlashSaleItemResponse.Availability.OUT_OF_STOCK;
        } else {
            availability = UserFlashSaleItemResponse.Availability.AVAILABLE;
        }

        String variantLabel = variant.options().stream()
                .sorted(Comparator.comparing(VariantCartView.OptionView::attributeId)
                        .thenComparing(VariantCartView.OptionView::valueId))
                .map(option -> option.name() + ": " + option.value())
                .collect(Collectors.joining(" · "));

        // Ưu tiên ảnh chính của variant, rồi ảnh đầu theo ID; thiếu ảnh thì dùng ảnh product.
        String imageUrl = variant.images().stream()
                .sorted(Comparator
                        .comparing(VariantCartView.ImageView::primary)
                        .reversed()
                        .thenComparing(VariantCartView.ImageView::id))
                .map(VariantCartView.ImageView::url)
                .filter(url -> url != null && !url.isBlank())
                .findFirst()
                .orElse(variant.productImageUrl());

        return new UserFlashSaleItemResponse(
                variant.productId(),
                variant.variantId(),
                variant.shopId(),
                variant.productName(),
                variantLabel,
                imageUrl,
                variant.price(),
                item.discountPercent(),
                FlashSaleRules.calculateSalePrice(
                        variant.price(),
                        item.discountPercent()
                ),
                item.registeredQuantity(),
                remainingQuantity,
                availability
        );
    }
}
