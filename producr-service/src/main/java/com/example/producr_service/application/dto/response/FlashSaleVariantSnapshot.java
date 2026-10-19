package com.example.producr_service.application.dto.response;

import com.example.producr_service.domain.model.FlashSaleItem;
import java.math.BigDecimal;
import java.util.List;
public record FlashSaleVariantSnapshot(Long variantId, Long shopId, String productName,
        boolean active, BigDecimal price, int stockQuantity, List<Attribute> attributes) {
    public FlashSaleItem.RegistrationVariant toRegistrationVariant() {
        return new FlashSaleItem.RegistrationVariant(
                variantId, shopId, productName, price, stockQuantity,
                attributes.stream().map(value -> new FlashSaleItem.Attribute(
                        value.name(), value.value())).toList());
    }

    public record Attribute(String name, String value) {}
}
