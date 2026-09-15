package com.example.auth_service.application.port.in;

import com.example.auth_service.application.DTO.repone.UserInternaInfoRespone;

public interface InternalGetUserInfoUseCase {
    public UserInternaInfoRespone getInfoUserInternal(String authorizationHeader);
}