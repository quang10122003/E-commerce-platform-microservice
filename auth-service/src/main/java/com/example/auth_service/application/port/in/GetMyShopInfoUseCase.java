package com.example.auth_service.application.port.in;

import com.example.auth_service.application.DTO.repone.PublicShopInfoResponse;

// Shop đang đăng nhập xem hồ sơ shop của mình; FE gọi thay cho API internal giữa các service.
public interface GetMyShopInfoUseCase {
    PublicShopInfoResponse getMyShopInfo(String authorizationHeader);
}
