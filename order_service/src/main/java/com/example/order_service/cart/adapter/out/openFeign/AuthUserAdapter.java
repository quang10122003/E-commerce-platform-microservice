package com.example.order_service.cart.adapter.out.openFeign;

import com.example.order_service.cart.adapter.client.AuthUserFeignClient;
import com.example.order_service.cart.application.dto.repone.UserInternaInfoRespone;
import com.example.order_service.cart.application.port.out.client.CurrentUserPort;
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
    public UserInternaInfoRespone getInfo() {
        return authUserFeignClient.getInFo();
    }
}
