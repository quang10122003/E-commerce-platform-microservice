package com.example.order_service.adapter.out.openFeign;

import com.example.common.exception.BusinessException;
import com.example.order_service.adapter.client.AuthUserFeignClient;
import com.example.order_service.application.dto.response.ShopStatusResponse;
import com.example.order_service.application.error.OrderError;
import com.example.order_service.application.port.out.client.ShopStatusPort;
import feign.FeignException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ShopStatusAdapter implements ShopStatusPort {
    private final AuthUserFeignClient authUserFeignClient;

    @Override
    public List<ShopStatusResponse> getShopStatuses(List<Long> shopIds) {
        if (shopIds.isEmpty()) {
            return List.of();
        }
        try {
            List<ShopStatusResponse> statuses = authUserFeignClient.getShopStatuses(shopIds);
            if (statuses == null || statuses.stream().anyMatch(status ->
                    status == null || status.shopId() == null || status.locked() == null)) {
                throw new BusinessException(OrderError.SHOP_INFO_UNAVAILABLE);
            }
            return statuses;
        } catch (FeignException exception) {
            throw new BusinessException(OrderError.SHOP_INFO_UNAVAILABLE, exception);
        }
    }
}
