package com.example.producr_service.application.service;

import com.example.common.exception.BusinessException;
import com.example.producr_service.application.dto.response.FlashSaleOfferView;
import com.example.producr_service.application.dto.response.VariantSaleResponse;
import com.example.producr_service.application.error.ProductError;
import com.example.producr_service.domain.service.FlashSaleRules;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.List;

// Dùng chung quy tắc xác định ưu đãi còn áp dụng cho chi tiết variant và giỏ hàng.
public class VariantSaleResolver {
    private final Clock clock;

    // Nhận Clock để thời gian kết thúc sale dùng đúng múi giờ cấu hình.
    public VariantSaleResolver(Clock clock) {
        this.clock = clock;
    }

    // Trả sale còn mua được; báo lỗi nếu lịch sale đang chạy bị chồng nhau.
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
