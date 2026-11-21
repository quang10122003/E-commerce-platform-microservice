package com.example.auth_service.application.service;

import com.example.auth_service.application.DTO.repone.UserAddressResponse;
import com.example.auth_service.application.DTO.request.CreateAddressRequest;
import com.example.auth_service.application.error.AuthError;
import com.example.auth_service.application.port.in.CreateMyAddressUseCase;
import com.example.auth_service.application.port.in.DeleteMyAddressUseCase;
import com.example.auth_service.application.port.in.GetMyAddressesUseCase;
import com.example.auth_service.application.port.in.SetDefaultAddressUseCase;
import com.example.auth_service.application.port.out.TokenServicePort;
import com.example.auth_service.application.port.out.UserAddressRepositoryPort;
import com.example.auth_service.application.port.out.UserRepositoryPort;
import com.example.auth_service.domain.model.UserAddress;
import com.example.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class UserAddressService implements GetMyAddressesUseCase, CreateMyAddressUseCase, DeleteMyAddressUseCase,
        SetDefaultAddressUseCase {
    private final UserAddressRepositoryPort userAddressRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;
    private final TokenServicePort tokenServicePort;

    @Override
    @Transactional(readOnly = true)
    public List<UserAddressResponse> getMyAddresses(String authorizationHeader) {
        return userAddressRepositoryPort.findByUserId(currentUserId(authorizationHeader)).stream()
                .map(UserAddressResponse::from)
                .toList();
    }

    @Override
    @Transactional
    public UserAddressResponse createMyAddress(String authorizationHeader, CreateAddressRequest request) {
        Long userId = lockCurrentUser(authorizationHeader);
        boolean makeDefault = Boolean.TRUE.equals(request.defaultAddress()) || !userAddressRepositoryPort.existsByUserId(userId);
        if (makeDefault) {
            userAddressRepositoryPort.clearDefault(userId);
        }
        UserAddress saved = userAddressRepositoryPort.save(UserAddress.create(userId, request.recipientName(),
                request.province(), request.ward(), request.detail(), request.phone(), makeDefault));
        log.info("Thêm địa chỉ: user_id={}, address_id={}, default={}", userId, saved.getId(), makeDefault);
        return UserAddressResponse.from(saved);
    }

    @Override
    @Transactional
    public UserAddressResponse setDefaultAddress(String authorizationHeader, Long addressId) {
        Long userId = lockCurrentUser(authorizationHeader);
        UserAddress address = findOwnedAddress(addressId, userId);
        userAddressRepositoryPort.clearDefault(userId);
        address.markDefault();
        UserAddress saved = userAddressRepositoryPort.save(address);
        log.info("Đặt địa chỉ mặc định: user_id={}, address_id={}", userId, addressId);
        return UserAddressResponse.from(saved);
    }

    // Xóa địa chỉ mặc định thì chuyển mặc định sang địa chỉ mới nhất còn lại để luôn có một địa chỉ mặc định.
    @Override
    @Transactional
    public void deleteMyAddress(String authorizationHeader, Long addressId) {
        Long userId = lockCurrentUser(authorizationHeader);
        UserAddress address = findOwnedAddress(addressId, userId);
        userAddressRepositoryPort.deleteById(addressId);
        if (address.isDefaultAddress()) {
            userAddressRepositoryPort.findByUserId(userId).stream().findFirst().ifPresent((UserAddress next) -> {
                next.markDefault();
                userAddressRepositoryPort.save(next);
            });
        }
        log.info("Xóa địa chỉ: user_id={}, address_id={}", userId, addressId);
    }

    // Lọc theo cả user id để không thao tác được trên địa chỉ của người khác.
    private UserAddress findOwnedAddress(Long addressId, Long userId) {
        return userAddressRepositoryPort.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new BusinessException(AuthError.ADDRESS_NOT_FOUND));
    }

    // Khóa dòng user để các thao tác đổi địa chỉ mặc định cùng lúc không tạo ra hai địa chỉ mặc định.
    private Long lockCurrentUser(String authorizationHeader) {
        Long userId = currentUserId(authorizationHeader);
        userRepositoryPort.findByIdForUpdate(userId)
                .orElseThrow(() -> new BusinessException(AuthError.USER_NOT_FOUND))
                .assertUserNotLocked();
        return userId;
    }

    private Long currentUserId(String authorizationHeader) {
        return tokenServicePort.getIdFromToken(tokenServicePort.extractBearerToken(authorizationHeader));
    }
}
