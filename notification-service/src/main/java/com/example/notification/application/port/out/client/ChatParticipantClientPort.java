package com.example.notification.application.port.out.client;

import com.example.notification.application.dto.response.ChatConversationSummaryResponse.ShopInfo;

import java.util.List;
import java.util.Map;

public interface ChatParticipantClientPort {
    Map<Long, ShopInfo> findShopInfosByIds(List<Long> shopIds);

    Map<Long, String> findCustomerNamesByIds(List<Long> customerIds);
}
