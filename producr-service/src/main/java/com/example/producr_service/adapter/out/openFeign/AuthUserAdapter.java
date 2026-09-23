package com.example.producr_service.adapter.out.openFeign;

import com.example.producr_service.adapter.client.AuthUserFeignClient;
import com.example.producr_service.application.dto.response.UserInternaInfoRespone;
import com.example.producr_service.application.port.out.client.CurrentUserPort;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@FieldDefaults(makeFinal = true,level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class AuthUserAdapter implements CurrentUserPort {
    AuthUserFeignClient authUserFeignClient;
    @Override
    public UserInternaInfoRespone getUserInfo() {
        return authUserFeignClient.getInfoUser();
    }
}
