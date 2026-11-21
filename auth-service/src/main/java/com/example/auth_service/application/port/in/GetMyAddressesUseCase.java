package com.example.auth_service.application.port.in;

import com.example.auth_service.application.DTO.repone.UserAddressResponse;

import java.util.List;

public interface GetMyAddressesUseCase {
    List<UserAddressResponse> getMyAddresses(String authorizationHeader);
}
