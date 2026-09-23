package com.example.auth_service.application.service;

import com.example.auth_service.application.DTO.repone.UserInternaInfoRespone;
import com.example.auth_service.application.error.AuthError;
import com.example.auth_service.application.mapper.UserInternalInfoResponseMapper;
import com.example.auth_service.application.port.in.InternalGetUserInfoUseCase;
import com.example.auth_service.application.port.out.TokenServicePort;
import com.example.auth_service.application.port.out.UserRepositoryPort;
import com.example.auth_service.domain.model.User;
import com.example.common.exception.BusinessException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class AuthServiceInternal implements InternalGetUserInfoUseCase {
    TokenServicePort tokenServicePort;
    UserRepositoryPort userRepositoryPort;

    @Override
    public UserInternaInfoRespone getInfoUserInternal(String authorizationHeader) {
        String token = tokenServicePort.extractBearerToken(authorizationHeader);
        String email = tokenServicePort.getEmailFromToken(token);

        User user = userRepositoryPort.findByEmail(email)
                .orElseThrow(() -> new BusinessException(AuthError.USER_NOT_FOUND));

        return UserInternalInfoResponseMapper.toResponse(user);

    }
}
