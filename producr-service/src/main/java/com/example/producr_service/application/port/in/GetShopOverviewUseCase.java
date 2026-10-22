package com.example.producr_service.application.port.in;

import com.example.producr_service.application.dto.response.ShopOverviewResponse;

public interface GetShopOverviewUseCase {
    ShopOverviewResponse getShopOverview(Long shopId);
}
