package com.example.producr_service.application.port.out.client;

import com.example.producr_service.application.dto.response.ShopStatusResponse;

import com.example.producr_service.application.dto.response.ShopInfoResponse;
import com.example.producr_service.application.dto.response.ShopProfileResponse;
import java.util.List;
import java.util.Optional;

public interface ShopInfoClientPort {
    List<ShopStatusResponse> getShopStatuses(
            List<Long> shopIds, Boolean locked);

    Optional<ShopProfileResponse> findPublicShopInfo(Long shopId);

    List<ShopInfoResponse> getShopInfos(List<Long> shopIds);
}
