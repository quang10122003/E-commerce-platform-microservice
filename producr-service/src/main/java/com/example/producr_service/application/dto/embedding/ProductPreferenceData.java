package com.example.producr_service.application.dto.embedding;

import java.math.BigDecimal;

// dữ liệu để nội bộ dùng để so sánh lấy sản giống theo sở thích để gợi ý
public record ProductPreferenceData(Long productId,
                                    Long categoryId,
                                    Long brandId,
                                    BigDecimal price) {
}
