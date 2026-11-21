package com.example.producr_service.application.service;

import com.example.common.exception.BusinessException;
import com.example.producr_service.application.dto.response.ShopStatusResponse;
import com.example.producr_service.application.dto.response.UserInternaInfoRespone;
import com.example.producr_service.application.error.ProductError;
import com.example.producr_service.application.port.out.client.CurrentUserPort;
import com.example.producr_service.application.port.out.client.ShopInfoClientPort;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
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

    // Trang shop không gắn với một sản phẩm nên vẫn hỏi auth-service; lỗi gọi được truyền lên để không coi shop là mở.
    public void requireAvailableShop(Long shopId) {
        boolean available = shopInfoClientPort.getShopStatuses(List.of(shopId), null).stream()
                .anyMatch(status -> shopId.equals(status.shopId()) && !status.locked());
        if (!available) {
            throw new BusinessException(ProductError.PRODUCT_UNAVAILABLE);
        }
    }
}
