package com.example.auth_service.application.port.in;

import com.example.auth_service.application.DTO.repone.UserLockResponse;

public interface ToggleUserLockUseCase {
    UserLockResponse toggleUserLock(Long userId);
}
