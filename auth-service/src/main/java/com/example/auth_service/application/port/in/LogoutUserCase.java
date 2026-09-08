package com.example.auth_service.application.port.in;

import com.example.auth_service.application.DTO.request.LogoutRequest;

public interface LogoutUserCase {
    void logout(LogoutRequest logoutRequest);
}
