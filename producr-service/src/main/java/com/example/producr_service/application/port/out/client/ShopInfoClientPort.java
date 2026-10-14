package com.example.producr_service.application.port.out.client;

import com.example.producr_service.application.dto.response.ShopInfoResponse;
import java.util.List;
import java.util.Optional;

public interface ShopInfoClientPort {
    // Đọc địa chỉ shop
    Optional<String> findPublicShopAddress(Long shopId);

    List<ShopInfoResponse> getShopInfos(List<Long> shopIds);
}
