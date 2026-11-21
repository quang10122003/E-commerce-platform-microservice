package com.example.order_service.application.port.in;

import com.example.order_service.application.dto.response.ShopOverviewResponse;

public interface GetShopOverviewUseCase {
    ShopOverviewResponse getShopOverview();
}
