package com.example.order_service.application.port.out.client;

import com.example.order_service.application.dto.response.ShopStatusResponse;
import java.util.List;

public interface ShopStatusPort {
    List<ShopStatusResponse> getShopStatuses(List<Long> shopIds);
}
