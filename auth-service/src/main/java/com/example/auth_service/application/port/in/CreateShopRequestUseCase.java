package com.example.auth_service.application.port.in;

import com.example.auth_service.application.DTO.repone.ShopRequestResponse;
import com.example.auth_service.application.DTO.request.CreateShopRequestRequest;

public interface CreateShopRequestUseCase {
    ShopRequestResponse createShopRequest(String authorizationHeader, CreateShopRequestRequest request);
}
