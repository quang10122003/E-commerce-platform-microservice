package com.example.notification.application.port.out.client;

import com.example.notification.application.dto.response.CurrentUserInfoResponse;

public interface CurrentUserPort {

    // Lấy thông tin người đang đăng nhập qua abstraction của Auth service.
    CurrentUserInfoResponse getUserInfo();
}
