package com.example.auth_service.application.port.in;

import com.example.auth_service.application.DTO.repone.AdminShopRequestResponse;

public interface RejectShopRequestUseCase {
    AdminShopRequestResponse rejectShopRequest(String authorizationHeader, Long requestId, String reason);
}
