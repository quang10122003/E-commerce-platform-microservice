package com.example.order_service.order.application.port.out.client;

import com.example.order_service.order.application.dto.checkout.ShopSnapshotDto;
import java.util.Optional;

public interface ShopSnapshotPort {
    Optional<ShopSnapshotDto> findById(Long shopId);
}
