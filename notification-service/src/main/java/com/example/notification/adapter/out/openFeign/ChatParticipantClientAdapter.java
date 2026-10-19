package com.example.notification.adapter.out.openFeign;

import com.example.notification.adapter.client.AuthUserFeignClient;
import com.example.notification.application.dto.response.ChatConversationSummaryResponse.ShopInfo;
import com.example.notification.application.port.out.client.ChatParticipantClientPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ChatParticipantClientAdapter implements ChatParticipantClientPort {
    private final AuthUserFeignClient authUserFeignClient;

    @Override
    public Map<Long, ShopInfo> findShopInfosByIds(List<Long> shopIds) {
        Map<Long, ShopInfo> shops = new HashMap<>();
        if (!shopIds.isEmpty()) {
            authUserFeignClient.getShopInfos(shopIds).forEach(shop -> shops.put(shop.shopId(),
                    new ShopInfo(shop.shopId(), shop.shopName(), shop.shopLogoUrl())));
        }
        return shops;
    }

    @Override
    public Map<Long, String> findCustomerNamesByIds(List<Long> customerIds) {
        Map<Long, String> names = new HashMap<>();
        if (!customerIds.isEmpty()) {
            authUserFeignClient.getUserDisplayInfosByIds(customerIds).forEach(user -> names.put(user.userId(), user.fullName()));
        }
        return names;
    }
}
