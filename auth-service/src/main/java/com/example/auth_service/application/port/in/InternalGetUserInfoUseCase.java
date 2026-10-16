package com.example.auth_service.application.port.in;

import com.example.auth_service.application.DTO.repone.ShopInfoResponse;
import com.example.auth_service.application.DTO.repone.UserDisplayInfoResponse;
import com.example.auth_service.application.DTO.repone.UserInternaInfoRespone;

import java.util.List;

public interface InternalGetUserInfoUseCase {
    List<UserDisplayInfoResponse> getUserDisplayInfosByIds(List<Long> userIds);
    public UserInternaInfoRespone getInfoUserInternal(String authorizationHeader);

    List<ShopInfoResponse> getShopInfos(List<Long> userIds);
}
