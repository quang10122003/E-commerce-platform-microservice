package com.example.auth_service.application.service;

import com.example.auth_service.application.DTO.repone.ShopAddressView;
import com.example.auth_service.application.DTO.repone.PublicShopInfoResponse;
import com.example.auth_service.application.DTO.repone.ShopInfoResponse;
import com.example.auth_service.application.DTO.repone.UserDisplayInfoResponse;
import com.example.auth_service.application.DTO.repone.UserInternaInfoRespone;
import com.example.auth_service.application.error.AuthError;
import com.example.auth_service.application.mapper.UserInternalInfoResponseMapper;
import com.example.auth_service.application.port.in.GetPublicShopInfoUseCase;
import com.example.auth_service.application.port.in.InternalGetUserInfoUseCase;
import com.example.auth_service.application.port.out.TokenServicePort;
import com.example.auth_service.application.port.out.UserRepositoryPort;
import com.example.auth_service.domain.model.User;
import com.example.common.exception.BusinessException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class AuthServiceInternal implements InternalGetUserInfoUseCase, GetPublicShopInfoUseCase {
    TokenServicePort tokenServicePort;
    UserRepositoryPort userRepositoryPort;

    @Override
    @Transactional(readOnly = true)
    public List<UserDisplayInfoResponse> getUserDisplayInfosByIds(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return List.of();
        }
        return userRepositoryPort.findUserDisplayInfosByIds(userIds.stream()
                .filter(Objects::nonNull).distinct().toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PublicShopInfoResponse getPublicShopInfo(Long shopId) {
        ShopAddressView shop = userRepositoryPort.findShopAddressById(shopId)
                .orElseThrow(() -> new BusinessException(AuthError.SHOP_NOT_FOUND));
        return new PublicShopInfoResponse(shop.shopId(), shop.shopName(), shop.shopAddress());
    }

    @Override
    public UserInternaInfoRespone getInfoUserInternal(String authorizationHeader) {
        String token = tokenServicePort.extractBearerToken(authorizationHeader);
        String email = tokenServicePort.getEmailFromToken(token);

        User user = userRepositoryPort.findByEmail(email)
                .orElseThrow(() -> new BusinessException(AuthError.USER_NOT_FOUND));

        return UserInternalInfoResponseMapper.toResponse(user);

    }

    @Override
    public List<ShopInfoResponse> getShopInfos(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return List.of();
        }
        return userRepositoryPort.findShopInfosByIds(userIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList());
    }
}
