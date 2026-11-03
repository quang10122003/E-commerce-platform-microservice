package com.example.order_service.order.adapter.out.openFeign;

import com.example.order_service.cart.adapter.client.AuthUserFeignClient;
import com.example.order_service.order.application.dto.checkout.ShopSnapshotDto;
import com.example.order_service.order.application.port.out.client.ShopSnapshotPort;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ShopSnapshotAdapter implements ShopSnapshotPort {
    private final AuthUserFeignClient client;

    @Override
    public Optional<ShopSnapshotDto> findById(Long shopId) {
        try {
            return Optional.ofNullable(client.getShopSnapshot(shopId));
        } catch (FeignException.NotFound exception) {
            return Optional.empty();
        }
    }
}
