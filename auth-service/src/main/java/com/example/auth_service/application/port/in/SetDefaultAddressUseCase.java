package com.example.auth_service.application.port.in;

import com.example.auth_service.application.DTO.repone.UserAddressResponse;

public interface SetDefaultAddressUseCase {
    UserAddressResponse setDefaultAddress(String authorizationHeader, Long addressId);
}
