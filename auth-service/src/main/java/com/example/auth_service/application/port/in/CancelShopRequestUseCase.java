package com.example.auth_service.application.port.in;

import com.example.auth_service.application.DTO.repone.ShopRequestResponse;

public interface CancelShopRequestUseCase {
    ShopRequestResponse cancelShopRequest(String authorizationHeader, Long requestId);
}
