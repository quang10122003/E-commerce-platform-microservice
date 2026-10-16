package com.example.producr_service.adapter.out.openFeign;

import com.example.producr_service.adapter.client.dto.PublicShopInfoResponse;
import com.example.producr_service.adapter.client.AuthUserFeignClient;
import com.example.producr_service.application.dto.response.ShopInfoResponse;
import com.example.producr_service.application.port.out.client.ShopInfoClientPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import feign.FeignException;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;

@Component("shopInfoClientPort")
@RequiredArgsConstructor
@Slf4j
public class ShopInfoClientAdapter implements ShopInfoClientPort {
    private final AuthUserFeignClient authUserFeignClient;

    // lấy thông tin shop public
    @Override
    public Optional<String> findPublicShopAddress(Long shopId) {
        try {
            PublicShopInfoResponse shop = authUserFeignClient.getPublicShopInfo(shopId);
            return shop == null ? Optional.empty()
                    : Optional.ofNullable(shop.location());
        } catch (FeignException exception) {
            log.warn("Không đọc được địa chỉ shop: shopId={}, httpStatus={}", shopId, exception.status());
            return Optional.empty();
        }
    }

    @Override
    public List<ShopInfoResponse> getShopInfos(List<Long> shopIds) {
        return authUserFeignClient.getShopInfos(shopIds);
    }
}
