package com.example.auth_service.application.port.in;

import com.example.auth_service.application.DTO.repone.ShopRequestResponse;

import java.util.Optional;

public interface GetMyShopRequestUseCase {
    // Đơn gần nhất của tài khoản; rỗng khi chưa từng nộp đơn.
    Optional<ShopRequestResponse> getMyLatestShopRequest(String authorizationHeader);
}
