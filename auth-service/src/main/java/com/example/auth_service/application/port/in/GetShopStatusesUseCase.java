package com.example.auth_service.application.port.in;

import com.example.auth_service.application.DTO.repone.ShopStatusResponse;
import java.util.List;

public interface GetShopStatusesUseCase {
    List<ShopStatusResponse> getShopStatuses(List<Long> shopIds, Boolean locked);
}
