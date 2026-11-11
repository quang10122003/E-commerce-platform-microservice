package com.example.auth_service.adapter.out.client;

import com.example.auth_service.application.error.AuthError;
import com.example.auth_service.application.port.out.OrderActivityPort;
import com.example.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderActivityAdapter implements OrderActivityPort {
    private final OrderActivityFeignClient orderActivityFeignClient;

    // Không kiểm tra được thì chặn xóa, tránh xóa tài khoản đang có đơn dở dang.
    @Override
    public boolean hasActiveOrders(Long userId) {
        try {
            return orderActivityFeignClient.hasActiveOrders(userId);
        } catch (RuntimeException exception) {
            throw new BusinessException(AuthError.ORDER_SERVICE_UNAVAILABLE, exception);
        }
    }
}
