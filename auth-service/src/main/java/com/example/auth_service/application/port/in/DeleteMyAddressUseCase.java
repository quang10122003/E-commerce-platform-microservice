package com.example.auth_service.application.port.in;

public interface DeleteMyAddressUseCase {
    void deleteMyAddress(String authorizationHeader, Long addressId);
}
