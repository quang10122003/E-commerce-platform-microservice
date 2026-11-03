package com.example.order_service.order.application.dto.repone;

import com.example.order_service.cart.application.dto.repone.VariantInfoResponse;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record CheckoutItemsResponse(List<Item> items) {
    public record Item(Long productId, Long variantId, String productName, String imageUrl,
                       Long shopId, String shopName, List<VariantInfoResponse.VariantOption> variantOptions,
                       int quantity, int saleQuantity, int regularQuantity, BigDecimal price, Sale sale) {
    }

    public record Sale(BigDecimal salePrice, OffsetDateTime endsAt) {
    }
}
