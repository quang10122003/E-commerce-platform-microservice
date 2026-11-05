package com.example.order_service.application.service;

import com.example.common.exception.BusinessException;
import com.example.order_service.application.dto.response.ProductVariantInfoResponse;
import com.example.order_service.application.dto.response.ShopStatusResponse;
import com.example.order_service.application.error.OrderError;
import com.example.order_service.application.port.out.client.ProductClientPort;
import com.example.order_service.application.port.out.client.ShopStatusPort;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        requireAvailableShops(variants.stream().map(ProductVariantInfoResponse::shopId).toList());
    }

    public void requireAvailableShops(Collection<Long> shopIds) {
        Map<Long, Boolean> locks = getShopLocks(shopIds);
        if (shopIds.stream().anyMatch(shopId -> !Boolean.FALSE.equals(locks.get(shopId)))) {
            throw new BusinessException(OrderError.PRODUCT_UNAVAILABLE);
        }
    }

    public Map<Long, Boolean> getShopLocks(Collection<Long> shopIds) {
        List<Long> ids = shopIds.stream().distinct().toList();
        Map<Long, Boolean> locks = new HashMap<>();
        for (int offset = 0; offset < ids.size(); offset += 500) {
            for (ShopStatusResponse status : shopStatusPort.getShopStatuses(
                    ids.subList(offset, Math.min(offset + 500, ids.size())))) {
                locks.put(status.shopId(), status.locked());
            }
        }
        return locks;
    }
}
