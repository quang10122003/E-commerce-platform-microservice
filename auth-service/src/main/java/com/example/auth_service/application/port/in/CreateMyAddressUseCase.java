package com.example.auth_service.application.port.in;

import com.example.auth_service.application.DTO.repone.UserAddressResponse;
import com.example.auth_service.application.DTO.request.CreateAddressRequest;

public interface CreateMyAddressUseCase {
    UserAddressResponse createMyAddress(String authorizationHeader, CreateAddressRequest request);
}
