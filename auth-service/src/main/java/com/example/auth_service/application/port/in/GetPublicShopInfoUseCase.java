package com.example.auth_service.application.port.in;

import com.example.auth_service.application.DTO.repone.PublicShopInfoResponse;

public interface GetPublicShopInfoUseCase {
    PublicShopInfoResponse getPublicShopInfo(Long shopId);
}
