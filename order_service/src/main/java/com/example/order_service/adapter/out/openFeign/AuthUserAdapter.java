package com.example.order_service.adapter.out.openFeign;

import com.example.order_service.adapter.client.AuthUserFeignClient;
import com.example.order_service.application.dto.response.UserInternalInfoResponse;
import com.example.order_service.application.port.out.client.CurrentUserPort;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
@RequiredArgsConstructor
public class AuthUserAdapter implements CurrentUserPort {
    AuthUserFeignClient authUserFeignClient;
    @Override
    public UserInternalInfoResponse getInfo() {
        return authUserFeignClient.getInFo();
    }
}
