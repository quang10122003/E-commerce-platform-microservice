package com.example.auth_service.application.port.in;

import com.example.auth_service.application.DTO.repone.ShopInfoResponse;
import com.example.auth_service.application.DTO.repone.UserInternaInfoRespone;
import java.util.List;

public interface InternalGetUserInfoUseCase {
    public UserInternaInfoRespone getInfoUserInternal(String authorizationHeader);

    List<ShopInfoResponse> getShopInfos(List<Long> userIds);
}