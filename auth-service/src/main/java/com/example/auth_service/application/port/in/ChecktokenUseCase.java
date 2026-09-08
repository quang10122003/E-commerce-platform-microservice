package com.example.auth_service.application.port.in;

import com.example.auth_service.application.DTO.repone.AccessTokenValidationResponse;

public interface ChecktokenUseCase {
    AccessTokenValidationResponse CheckToken(String authorizationHeader);
}
