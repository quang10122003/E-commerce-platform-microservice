package com.example.producr_service.adapter.out.openFeign;

import com.example.producr_service.adapter.client.AuthUserFeignClient;
import com.example.producr_service.application.dto.response.ShopInfoResponse;
import com.example.producr_service.application.port.out.client.ShopInfoClientPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.List;

@Component("shopInfoClientPort")
@RequiredArgsConstructor
public class ShopInfoClientAdapter implements ShopInfoClientPort {
    private final AuthUserFeignClient authUserFeignClient;

    @Override
    public List<ShopInfoResponse> getShopInfos(List<Long> shopIds) {
        return authUserFeignClient.getShopInfos(shopIds);
    }
}
