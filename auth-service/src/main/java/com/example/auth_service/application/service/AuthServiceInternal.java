package com.example.auth_service.application.service;

import com.example.auth_service.application.DTO.repone.UserInternaInfoRespone;
import com.example.auth_service.application.port.in.InternalGetUserInfoUseCase;
import com.example.auth_service.application.port.out.TokenServicePort;
import com.example.auth_service.application.port.out.UserRepositoryPort;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class AuthServiceInternal implements InternalGetUserInfoUseCase {
    TokenServicePort tokenServicePort;

    @Override
    public UserInternaInfoRespone getInfoUserInternal(String authorizationHeader) {
        String token  = tokenServicePort.extractBearerToken(authorizationHeader);
        Long UserId = tokenServicePort.getIdFromToken(token);
        return UserInternaInfoRespone.builder().userId(UserId).build();

    }
}
