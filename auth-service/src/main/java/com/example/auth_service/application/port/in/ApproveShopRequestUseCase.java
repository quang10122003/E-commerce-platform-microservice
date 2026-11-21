package com.example.auth_service.application.port.in;

import com.example.auth_service.application.DTO.repone.AdminShopRequestResponse;

public interface ApproveShopRequestUseCase {
    AdminShopRequestResponse approveShopRequest(String authorizationHeader, Long requestId);
}
