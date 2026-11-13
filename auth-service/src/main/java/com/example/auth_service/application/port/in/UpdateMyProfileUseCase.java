package com.example.auth_service.application.port.in;

import com.example.auth_service.application.DTO.repone.UserProfileResponse;
import com.example.auth_service.application.DTO.request.UpdateProfileRequest;

public interface UpdateMyProfileUseCase {
    UserProfileResponse updateMyProfile(String authorizationHeader, UpdateProfileRequest request);
}
