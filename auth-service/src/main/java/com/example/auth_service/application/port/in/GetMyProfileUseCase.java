package com.example.auth_service.application.port.in;

import com.example.auth_service.application.DTO.repone.UserProfileResponse;

public interface GetMyProfileUseCase {
    UserProfileResponse getMyProfile(String authorizationHeader);
}
