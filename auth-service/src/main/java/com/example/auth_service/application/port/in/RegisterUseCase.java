package com.example.auth_service.application.port.in;

import com.example.auth_service.application.DTO.repone.AuthResponse;
import com.example.auth_service.application.DTO.request.RegisterRquest;

public interface RegisterUseCase {
    AuthResponse register(RegisterRquest request);
}
