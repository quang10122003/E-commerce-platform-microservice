package com.example.notification.adapter.out.openFeign;

import com.example.common.error.AuthorizationError;
import com.example.common.exception.BusinessException;
import com.example.notification.adapter.client.CheckoutFeignClient;
import com.example.notification.application.port.out.client.CheckoutAccessPort;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CheckoutAccessAdapter implements CheckoutAccessPort {
    private final CheckoutFeignClient checkoutFeignClient;

    @Override
    public void checkAccess(Long checkoutId) {
        try {
            checkoutFeignClient.checkAccess(checkoutId);
        } catch (FeignException exception) {
            if (exception.status() == 403 || exception.status() == 404) {
                throw new BusinessException(AuthorizationError.ACCESS_DENIED);
            }
            throw new BusinessException(AuthorizationError.ACCESS_CONTROL_UNAVAILABLE);
        }
    }
}
