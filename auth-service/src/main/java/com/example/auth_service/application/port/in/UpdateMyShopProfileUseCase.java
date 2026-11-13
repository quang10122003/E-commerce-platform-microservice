package com.example.auth_service.application.port.in;

import com.example.auth_service.application.DTO.repone.PublicShopInfoResponse;
import com.example.auth_service.application.DTO.request.UpdateShopProfileRequest;

public interface UpdateMyShopProfileUseCase {
    PublicShopInfoResponse updateMyShopProfile(String authorizationHeader, UpdateShopProfileRequest request);
}
