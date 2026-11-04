package com.example.order_service.application.port.out.client;

import com.example.order_service.application.dto.response.UserInternalInfoResponse;

public interface CurrentUserPort {
    UserInternalInfoResponse getInfo();
}
