package com.example.order_service.order.application.service;

import com.example.common.exception.BusinessException;
import com.example.order_service.cart.application.dto.repone.VariantInfoResponse;
import com.example.order_service.cart.application.port.out.client.ProductClientPort;
import com.example.order_service.order.application.dto.request.CheckoutItemsRequest;
import com.example.order_service.order.application.dto.repone.CheckoutItemsResponse;
import com.example.order_service.order.application.port.in.GetCheckoutItemsUseCase;
import com.example.order_service.order.domain.error.OrderDomainError;
import com.example.order_service.order.application.error.OrderError;
import com.example.order_service.cart.application.error.CartQuoteError;
import com.example.order_service.cart.domain.model.ProductStatus;
import lombok.RequiredArgsConstructor;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class CheckoutItemsService implements GetCheckoutItemsUseCase {
    private final ProductClientPort productClientPort;
    private final Clock clock;

    @Override
    public CheckoutItemsResponse getCheckoutItems(CheckoutItemsRequest request) {
        List<Long> variantIds = request.items().stream().map(CheckoutItemsRequest.Item::variantId).toList();
        if (variantIds.stream().distinct().count() != variantIds.size()) {
            throw new BusinessException(OrderDomainError.DUPLICATE_ITEM);
        }
        Map<Long, VariantInfoResponse> infos = productClientPort.getVariantInfo(variantIds).stream()
                .collect(Collectors.toMap(VariantInfoResponse::productVariantId, Function.identity()));
        if (!infos.keySet().containsAll(variantIds)) {
            throw new BusinessException(CartQuoteError.VARIANT_NOT_FOUND);
        }
        if (infos.values().stream().anyMatch(info -> info.status() != ProductStatus.ACTIVE)) {
            throw new BusinessException(OrderError.CHECKOUT_PRODUCT_UNAVAILABLE);
        }
        if (infos.values().stream().anyMatch(info -> info.shopName() == null)) {
            throw new BusinessException(OrderError.CHECKOUT_SHOP_NOT_FOUND);
        }
        Instant now = clock.instant();
        return new CheckoutItemsResponse(request.items().stream().map(item -> {
            VariantInfoResponse info = infos.get(item.variantId());
            VariantInfoResponse.Sale sale = info.sale();
            int saleQuantity = sale != null && sale.endsAt().toInstant().isAfter(now)
                    ? (int) Math.min(item.quantity(), Math.max(0L, sale.remainingQuantity())) : 0;
            return new CheckoutItemsResponse.Item(info.productId(), item.variantId(), info.productName(),
                    info.imageUrl(), info.shopId(), info.shopName(), info.variantOptions(), item.quantity(),
                    saleQuantity, item.quantity() - saleQuantity, info.price(), saleQuantity == 0 ? null
                    : new CheckoutItemsResponse.Sale(sale.salePrice(),
                            sale.endsAt().atZoneSameInstant(clock.getZone()).toOffsetDateTime()));
        }).toList());
    }
}
