package com.example.producr_service.application.service;

import com.example.common.exception.BusinessException;
import com.example.producr_service.application.dto.response.FlashSaleOfferView;
import com.example.producr_service.application.dto.response.VariantSaleResponse;
import com.example.producr_service.application.error.ProductError;
import com.example.producr_service.domain.service.FlashSaleRules;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.List;

public class VariantSaleResolver {
    private final Clock clock;

    // Offset kết thúc sale phải dùng múi giờ của Clock, không dùng múi giờ mặc định của máy.
    public VariantSaleResolver(Clock clock) {
        this.clock = clock;
    }

    public VariantSaleResponse.Sale resolve(BigDecimal price, int stockQuantity, List<FlashSaleOfferView> offers) {
        // Không chọn ngẫu nhiên ưu đãi khi dữ liệu vi phạm quy tắc lịch sale không chồng nhau.
        if (offers.size() > 1) {
            throw new BusinessException(ProductError.FLASH_SALE_SCHEDULE_CONFLICT);
        }
        if (offers.isEmpty()) {
            return null;
        }
        FlashSaleOfferView offer = offers.getFirst();
        if (stockQuantity <= 0 || offer.soldQuantity() >= offer.registeredQuantity()) {
            return null;
        }
        return new VariantSaleResponse.Sale(offer.discountPercent(),
                FlashSaleRules.calculateSalePrice(price, offer.discountPercent()),
                offer.endsAt().atZone(clock.getZone()).toOffsetDateTime());
    }
}
