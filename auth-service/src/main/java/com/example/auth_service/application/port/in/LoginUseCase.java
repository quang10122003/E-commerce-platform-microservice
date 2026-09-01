package com.example.auth_service.application.port.in;

import com.example.auth_service.application.DTO.repone.AuthResponse;
import com.example.auth_service.application.DTO.request.LoginRequest;

public interface LoginUseCase {
     AuthResponse login(LoginRequest request);
}
