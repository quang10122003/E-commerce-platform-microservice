package com.example.auth_service.application.port.in;

import com.example.auth_service.application.DTO.request.ChangePasswordRequest;

public interface ChangePasswordUseCase {
    void changePassword(String authorizationHeader, ChangePasswordRequest request);
}
