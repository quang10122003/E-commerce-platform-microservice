package com.example.order_service.application.service;

import com.example.common.exception.BusinessException;
import com.example.order_service.application.dto.response.ProductVariantInfoResponse;
import com.example.order_service.application.error.OrderError;
import com.example.order_service.application.port.out.client.ProductClientPort;
import com.example.order_service.application.port.out.client.ShopStatusPort;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ShopAvailabilityService {
    private final ProductClientPort productClientPort;
    private final ShopStatusPort shopStatusPort;

    public void requireAvailableVariants(List<Long> variantIds) {
        List<ProductVariantInfoResponse> variants = productClientPort.getVariantInfo(variantIds);
        Set<Long> foundIds = variants.stream().map(ProductVariantInfoResponse::productVariantId).collect(Collectors.toSet());
        if (!foundIds.containsAll(variantIds)) {
            throw new BusinessException(OrderError.PRODUCT_UNAVAILABLE);
        }
        if (variants.stream().anyMatch(ProductVariantInfoResponse::shopLocked)) {
            throw new BusinessException(OrderError.PRODUCT_UNAVAILABLE);
        }
    }

    // Shop tự thao tác đơn có thể không còn sản phẩm nên vẫn hỏi auth-service; thiếu trạng thái coi như bị khóa.
    public boolean isShopLocked(Long shopId) {
        return shopStatusPort.getShopStatuses(List.of(shopId)).stream()
                .noneMatch(status -> shopId.equals(status.shopId()) && Boolean.FALSE.equals(status.locked()));
    }
}
