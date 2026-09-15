package com.example.producr_service.application.port.out;

import com.example.producr_service.application.dto.response.UserInternaInfoRespone;

public interface CurrentUserPort {
    UserInternaInfoRespone getCurrentUserId();
}