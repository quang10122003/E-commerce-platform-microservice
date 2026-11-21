package com.example.order_service.application.service;

import com.example.common.exception.BusinessException;
import com.example.order_service.application.dto.response.ProductVariantInfoResponse;
import com.example.order_service.application.port.out.client.ProductClientPort;
import com.example.order_service.application.dto.request.CheckoutItemsRequest;
import com.example.order_service.application.dto.response.CheckoutItemsResponse;
import com.example.order_service.application.port.in.GetCheckoutItemsUseCase;
import com.example.order_service.application.port.in.GetCheckoutQuoteUseCase;
import com.example.order_service.application.dto.request.CheckoutQuoteRequest;
import com.example.order_service.application.dto.request.ProductQuoteRequest;
import com.example.order_service.application.dto.response.CheckoutQuoteResponse;
import com.example.order_service.application.dto.response.ProductQuoteResponse;
import com.example.order_service.domain.error.OrderDomainError;
import com.example.order_service.application.error.OrderError;
import com.example.order_service.application.error.ProductQuoteError;
import com.example.order_service.domain.model.ProductStatus;
import lombok.RequiredArgsConstructor;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
// Dữ liệu trang checkout trước khi đặt hàng: danh sách sản phẩm và tổng tiền tạm tính.
public class CheckoutItemsService implements GetCheckoutItemsUseCase, GetCheckoutQuoteUseCase {
    private final ProductClientPort productClientPort;
    private final Clock clock;
    private final ShopAvailabilityService shopAvailabilityService;

    @Override
    public CheckoutItemsResponse getCheckoutItems(CheckoutItemsRequest request) {
        List<Long> variantIds = request.items().stream().map(CheckoutItemsRequest.Item::variantId).toList();
        if (variantIds.stream().distinct().count() != variantIds.size()) {
            throw new BusinessException(OrderDomainError.DUPLICATE_ITEM);
        }
        Map<Long, ProductVariantInfoResponse> infos = productClientPort.getVariantInfo(variantIds).stream()
                .collect(Collectors.toMap(ProductVariantInfoResponse::productVariantId, Function.identity()));
        if (!infos.keySet().containsAll(variantIds)) {
            throw new BusinessException(ProductQuoteError.VARIANT_NOT_FOUND);
        }
        if (infos.values().stream().anyMatch(ProductVariantInfoResponse::shopLocked)) {
            throw new BusinessException(OrderError.PRODUCT_UNAVAILABLE);
        }
        if (infos.values().stream().anyMatch(info -> info.status() != ProductStatus.ACTIVE)) {
            throw new BusinessException(OrderError.CHECKOUT_PRODUCT_UNAVAILABLE);
        }
        if (infos.values().stream().anyMatch(info -> info.shopName() == null)) {
            throw new BusinessException(OrderError.CHECKOUT_SHOP_NOT_FOUND);
        }
        Instant now = clock.instant();
        return new CheckoutItemsResponse(request.items().stream().map(item -> {
            ProductVariantInfoResponse info = infos.get(item.variantId());
            ProductVariantInfoResponse.Sale sale = info.sale();
            int saleQuantity = sale != null && sale.endsAt().toInstant().isAfter(now)
                    ? (int) Math.min(item.quantity(), Math.max(0L, sale.remainingQuantity())) : 0;
            return new CheckoutItemsResponse.Item(info.productId(), item.variantId(), info.productName(),
                    info.imageUrl(), info.shopId(), info.shopName(), info.variantOptions(), item.quantity(),
                    saleQuantity, item.quantity() - saleQuantity, info.price(), saleQuantity == 0 ? null
                    : new CheckoutItemsResponse.Sale(sale.salePrice(),
                            sale.endsAt().atZoneSameInstant(clock.getZone()).toOffsetDateTime()));
        }).toList());
    }

    @Override
    public CheckoutQuoteResponse getCheckoutQuote(CheckoutQuoteRequest request) {
        shopAvailabilityService.requireAvailableVariants(request.items().stream()
                .map(CheckoutQuoteRequest.Item::variantId).distinct().toList());
        ProductQuoteRequest pricingRequest = new ProductQuoteRequest(request.items().stream()
                .map(item -> new ProductQuoteRequest.Item(item.variantId(), item.quantity()))
                .toList());
        ProductQuoteResponse quote = productClientPort.getProductQuote(pricingRequest);
        return new CheckoutQuoteResponse(quote.totalAmount());
    }
}
