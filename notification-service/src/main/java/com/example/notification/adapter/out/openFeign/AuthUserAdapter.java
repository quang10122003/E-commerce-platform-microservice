package com.example.notification.adapter.out.openFeign;

import com.example.notification.adapter.client.AuthUserFeignClient;
import com.example.notification.application.dto.response.CurrentUserInfoResponse;
import com.example.notification.application.port.out.client.CurrentUserPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthUserAdapter implements CurrentUserPort {
    private final AuthUserFeignClient authUserFeignClient;

    // Chuyển dữ liệu tích hợp sang DTO application, không ghi log thông tin người dùng.
    @Override
    public CurrentUserInfoResponse getUserInfo() {
        return  authUserFeignClient.getInfoUser();

    }
}
