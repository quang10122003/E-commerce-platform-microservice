package com.example.producr_service.application.service;

import com.example.common.exception.BusinessException;
import com.example.producr_service.application.dto.response.ShopStatusResponse;
import com.example.producr_service.application.dto.response.UserInternaInfoRespone;
import com.example.producr_service.application.error.ProductError;
import com.example.producr_service.application.port.out.client.CurrentUserPort;
import com.example.producr_service.application.port.out.client.ShopInfoClientPort;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class ShopAccessService {
    private final ShopInfoClientPort shopInfoClientPort;
    private final CurrentUserPort currentUserPort;

    // Trả thông tin người bán hiện tại; báo lỗi nếu shop không tồn tại hoặc bị khóa.
    public UserInternaInfoRespone requireSellerAccess() {
        UserInternaInfoRespone user = currentUserPort.getUserInfo();
        List<ShopStatusResponse> statuses = shopInfoClientPort.getShopStatuses(List.of(user.userId()), null);
        ShopStatusResponse status = statuses.stream().filter(value -> user.userId().equals(value.shopId()))
                .findFirst().orElseThrow(() -> new BusinessException(ProductError.SHOP_NOT_FOUND));
        if (status.locked()) {
            throw new BusinessException(ProductError.SHOP_LOCKED);
        }
        return user;
    }

    // Lấy ID của tất cả shop bị khóa.
    public Set<Long> findLockedShopIds() {
        return shopInfoClientPort.getShopStatuses(null, true).stream()
                .filter(ShopStatusResponse::locked)
                .map(ShopStatusResponse::shopId).collect(Collectors.toSet());
    }

    // Lọc ID shop không bị khóa; truyền lỗi nếu không kiểm tra được trạng thái.
    public Set<Long> findAvailableShopIds(Collection<Long> shopIds) {
        return findAvailableShopIds(shopIds, false);
    }

    // Lọc shop để hiển thị; coi shop không bị khóa khi dịch vụ trạng thái không khả dụng.
    public Set<Long> findAvailableShopIdsForDisplay(Collection<Long> shopIds) {
        return findAvailableShopIds(shopIds, true);
    }

    // Kiểm tra từng lô tối đa 500 ID và áp dụng chính sách khi dịch vụ trạng thái lỗi.
    private Set<Long> findAvailableShopIds(Collection<Long> shopIds, boolean allowUnavailableBatch) {
        List<Long> ids = shopIds.stream().distinct().toList();
        Set<Long> available = new HashSet<>();
        for (int offset = 0; offset < ids.size(); offset += 500) {
            List<Long> batch = ids.subList(offset, Math.min(offset + 500, ids.size()));
            try {
                available.addAll(shopInfoClientPort.getShopStatuses(batch, false).stream()
                        .filter(status -> !status.locked())
                        .map(ShopStatusResponse::shopId).toList());
            } catch (BusinessException exception) {
                if (!allowUnavailableBatch || exception.getErrorCode() != ProductError.SHOP_INFO_UNAVAILABLE) {
                    throw exception;
                }
                available.addAll(batch);
                log.warn("Mặc định shop không bị khóa khi không kiểm tra được trạng thái: shop_count={}", batch.size());
            }
        }
        return available;
    }

    // Báo lỗi nếu có shop bị khóa, không tồn tại hoặc không kiểm tra được trạng thái.
    public void requireAvailableShops(Collection<Long> shopIds) {
        if (!findAvailableShopIds(shopIds).containsAll(shopIds)) {
            throw new BusinessException(ProductError.PRODUCT_UNAVAILABLE);
        }
    }
}
