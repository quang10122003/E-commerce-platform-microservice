package com.example.producr_service.application.service;

import com.example.producr_service.domain.model.FlashSaleCampaign;
import com.example.producr_service.domain.model.FlashSaleItem;
import com.example.producr_service.domain.model.FlashSaleCampaignStatus;
import com.example.common.exception.BusinessException;
import com.example.producr_service.application.dto.request.*;
import com.example.producr_service.application.dto.response.*;
import com.example.producr_service.application.error.ProductError;
import com.example.producr_service.application.port.in.*;
import com.example.producr_service.application.port.out.client.CurrentUserPort;
import com.example.producr_service.application.port.out.repo.*;
import com.example.producr_service.domain.service.FlashSaleRules;
import com.example.producr_service.domain.model.ProductStatus;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class FlashSaleItemService implements RegisterFlashSaleItemUseCase, PreviewFlashSalePriceUseCase,GetUserFlashSaleItemsUseCase, GetVariantSaleUseCase, GetShopFlashSaleItemsUseCase, RemoveShopFlashSaleItemUseCase, UpdateShopFlashSaleItemUseCase {
     CurrentUserPort currentUser;
     FlashSaleCampaignRepositoryPort flashSaleCampaignRepositoryPort;
     FlashSaleItemRepositoryPort flashSaleItemRepositoryPort;
     Clock clock;
     ProductVariantRepoPort productVariantRepoPort;
     VariantSaleResolver variantSaleResolver;

    @Override
    @Transactional(readOnly = true)
    public List<ShopFlashSaleItemResponse> getShopItems(Long campaignId, String keyword) {
        Long shopId = currentUser.getUserInfo().userId();
        FlashSaleCampaignView campaign = flashSaleCampaignRepositoryPort.findViewById(campaignId)
                .orElseThrow(() -> new BusinessException(ProductError.FLASH_SALE_CAMPAIGN_NOT_FOUND));
        if (campaign.status() != FlashSaleCampaignStatus.DRAFT) {
            throw new BusinessException(ProductError.FLASH_SALE_CAMPAIGN_NOT_DRAFT);
        }
        List<FlashSaleItem> items = flashSaleItemRepositoryPort.findShopItems(campaignId, shopId);
        List<Long> variantIds = items.stream().map(FlashSaleItem::variantId)
                .filter(Objects::nonNull).distinct().toList();
        Map<Long, FlashSaleVariantSnapshot> variantsById = flashSaleItemRepositoryPort.findVariants(variantIds)
                .stream().collect(Collectors.toMap(FlashSaleVariantSnapshot::variantId, Function.identity()));
        String searchKeyword = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
        return items.stream().map(item -> toShopItemResponse(item, variantsById))
                .filter(item -> item.productName().toLowerCase(Locale.ROOT).contains(searchKeyword)).toList();
    }

    private ShopFlashSaleItemResponse toShopItemResponse(FlashSaleItem item,
            Map<Long, FlashSaleVariantSnapshot> variantsById) {
        if (item.variantId() == null) {
            return new ShopFlashSaleItemResponse(item.id(), item.variantId(), item.productName(),
                    item.attributes().stream()
                            .map(attribute -> new ShopFlashSaleItemResponse.VariantOption(attribute.name(), attribute.value()))
                            .toList(),
                    item.registeredQuantity(), null, item.status(), item.discountPercent(), null, true);
        }
        FlashSaleVariantSnapshot variant = variantsById.get(item.variantId());
        if (variant == null) {
            throw new BusinessException(ProductError.PRODUCT_VARIANT_NOT_FOUND);
        }
        return new ShopFlashSaleItemResponse(item.id(), item.variantId(), variant.productName(),
                variant.attributes().stream()
                        .map(attribute -> new ShopFlashSaleItemResponse.VariantOption(attribute.name(), attribute.value()))
                        .toList(),
                item.registeredQuantity(), variant.stockQuantity(), item.status(), item.discountPercent(),
                FlashSaleRules.calculateSalePrice(variant.price(), item.discountPercent()), false);
    }

    @Override
    @Transactional(readOnly = true)
    public VariantSaleResponse getVariantSale(Long productId, Long variantId) {
        VariantPriceView variant = productVariantRepoPort.findPriceView(productId, variantId)
                .orElseThrow(() -> new BusinessException(ProductError.PRODUCT_VARIANT_NOT_FOUND));
        // Không hoạt động và không tồn tại là hai lỗi khác nhau, không gộp chung khi tra ưu đãi.
        if (variant.productStatus() != ProductStatus.ACTIVE) {
            throw new BusinessException(ProductError.PRODUCT_INACTIVE);
        }

        LocalDateTime now = LocalDateTime.now(clock);
        List<FlashSaleOfferView> offers = flashSaleItemRepositoryPort.findOngoingOffers(productId, variantId, now);
        return new VariantSaleResponse(variantId, variant.price(),
                variantSaleResolver.resolve(variant.price(), variant.stockQuantity(), offers));
    }

    // Phải khóa và tải đủ items trước khi thêm đăng ký để không ghi đè cập nhật đồng thời.
    @Override
    @Transactional
    public void register(Long campaignId, RegisterFlashSaleItemRequest request) {
        Long shopId = currentUser.getUserInfo().userId();
        FlashSaleCampaign campaign = flashSaleCampaignRepositoryPort.findByIdForUpdate(campaignId)
                .orElseThrow(() -> new BusinessException(ProductError.FLASH_SALE_CAMPAIGN_NOT_FOUND));

        FlashSaleVariantSnapshot variant = ownedVariant(request.variantId(), shopId);
        LocalDateTime now = LocalDateTime.now(clock);
        campaign.addItem(variant.toRegistrationVariant(),
                request.discountPercent(), request.registeredQuantity(), now);
        flashSaleCampaignRepositoryPort.save(campaign);
    }

    @Override
    @Transactional
    public void removeItem(Long campaignId, Long itemId) {
        Long shopId = currentUser.getUserInfo().userId();
        FlashSaleCampaign campaign = flashSaleCampaignRepositoryPort.findByIdForUpdate(campaignId)
                .orElseThrow(() -> new BusinessException(ProductError.FLASH_SALE_CAMPAIGN_NOT_FOUND));
        campaign.removeItem(itemId, shopId, LocalDateTime.now(clock));
        flashSaleCampaignRepositoryPort.save(campaign);
    }

    @Override
    @Transactional
    public void updateItem(Long campaignId, Long itemId, UpdateShopFlashSaleItemRequest request) {
        Long shopId = currentUser.getUserInfo().userId();

        FlashSaleCampaign campaign = flashSaleCampaignRepositoryPort.findByIdForUpdate(campaignId)
                .orElseThrow(() -> new BusinessException(ProductError.FLASH_SALE_CAMPAIGN_NOT_FOUND));

        FlashSaleItem item = campaign.getOwnedItem(itemId, shopId);

        if (item.variantId() == null) {
            throw new BusinessException(ProductError.PRODUCT_VARIANT_NOT_FOUND);
        }
        FlashSaleVariantSnapshot variant = ownedVariant(item.variantId(), shopId);

        campaign.updateItemRegistration(itemId, shopId, variant.toRegistrationVariant(), request.discountPercent(),
                request.registeredQuantity(), LocalDateTime.now(clock));
        flashSaleCampaignRepositoryPort.save(campaign);
    }

    @Override
    @Transactional(readOnly = true)
    public FlashSalePriceResponse previewPrice(Long variantId, PreviewFlashSalePriceRequest request) {
        FlashSaleVariantSnapshot variant = ownedVariant(variantId, currentUser.getUserInfo().userId());
        return new FlashSalePriceResponse(
                FlashSaleRules.calculateSalePrice(variant.price(), request.discountPercent()));
    }

    private FlashSaleVariantSnapshot ownedVariant(Long variantId, Long shopId) {
        FlashSaleVariantSnapshot variant = flashSaleItemRepositoryPort.findVariant(variantId)
                .orElseThrow(() -> new BusinessException(ProductError.PRODUCT_VARIANT_NOT_FOUND));
        if (!variant.shopId().equals(shopId)) {
            throw new BusinessException(ProductError.PRODUCT_ACCESS_DENIED);
        }
        if (!variant.active()) {
            throw new BusinessException(ProductError.FLASH_SALE_PRODUCT_INACTIVE);
        }
        return variant;
    }

    @Transactional(readOnly = true)
    @Override
    public UserFlashSaleResponse getItems(Long categoryId) {
        return getSaleItems(categoryId, false);
    }

    @Override
    @Transactional(readOnly = true)
    public UserFlashSaleResponse getTopSellingItems() {
        return getSaleItems(null, true);
    }

    private UserFlashSaleResponse getSaleItems(Long categoryId, boolean topSellingOnly) {
        LocalDateTime now = LocalDateTime.now(clock);
        List<UserFlashSaleItemView> items = flashSaleItemRepositoryPort.findItemsInOngoingCampaigns(categoryId, now);
        if (items.isEmpty()) {
            return new UserFlashSaleResponse(null, List.of());
        }

        List<UserFlashSaleItemResponse> cards;
        if (topSellingOnly) {
            // Lọc trước limit để item hết quota hoặc tồn kho không chiếm chỗ trong top 5.
            cards = items.stream()
                    .sorted(Comparator.comparingLong(UserFlashSaleItemView::soldQuantity)
                            .reversed()
                            .thenComparing(item -> item.variant().variantId()))
                    .map(this::toUserItemResponse)
                    .filter(item -> item.availability() == UserFlashSaleItemResponse.Availability.AVAILABLE)
                    .limit(5)
                    .toList();
        } else {
            cards = items.stream().map(this::toUserItemResponse).toList();
        }

        // Lịch sale không chồng nhau nên các item có chung thời gian hết hạn.
        return new UserFlashSaleResponse(
                items.getFirst().endsAt().atZone(clock.getZone()).toOffsetDateTime(),
                cards
        );
    }


    private UserFlashSaleItemResponse toUserItemResponse(UserFlashSaleItemView item) {
        VariantCartView variant = item.variant();

        // Quota sale không thay thế tồn kho thực tế; số lượng mua phải thỏa cả hai giới hạn.
        long quotaRemaining = Math.max(
                0L,
                (long) item.registeredQuantity() - item.soldQuantity()
        );
        long stockAvailable = Math.max(0L, variant.stockQuantity());

        long remainingQuantity = Math.min(quotaRemaining, stockAvailable);

        // Hết quota được ưu tiên vì nhập thêm hàng cũng không khôi phục suất sale.
        UserFlashSaleItemResponse.Availability availability;
        if (quotaRemaining == 0) {
            availability = UserFlashSaleItemResponse.Availability.SOLD_OUT;
        } else if (stockAvailable == 0) {
            availability = UserFlashSaleItemResponse.Availability.OUT_OF_STOCK;
        } else {
            availability = UserFlashSaleItemResponse.Availability.AVAILABLE;
        }

        String variantLabel = variant.options().stream()
                .sorted(Comparator.comparing(VariantCartView.OptionView::attributeId)
                        .thenComparing(VariantCartView.OptionView::valueId))
                .map(option -> option.name() + ": " + option.value())
                .collect(Collectors.joining(" · "));

        // Thiếu ảnh variant không được làm mất ảnh card, nên dùng ảnh product làm phương án dự phòng.
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
