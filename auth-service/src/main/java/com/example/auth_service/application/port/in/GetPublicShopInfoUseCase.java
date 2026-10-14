package com.example.auth_service.application.port.in;

import com.example.auth_service.application.DTO.repone.PublicShopInfoResponse;

public interface GetPublicShopInfoUseCase {
    // Lấy tên và địa chỉ shop công khai mà không cần người xem đăng nhập.
    PublicShopInfoResponse getPublicShopInfo(Long shopId);
}
