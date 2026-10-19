package com.example.notification.application.port.out.client;

import com.example.notification.application.dto.response.CurrentUserInfoResponse;

public interface CurrentUserPort {

    CurrentUserInfoResponse getUserInfo();
}
