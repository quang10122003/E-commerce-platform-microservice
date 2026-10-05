package com.example.producr_service.application.port.out.client;

import com.example.producr_service.application.dto.response.ShopInfoResponse;
import java.util.List;

public interface ShopInfoClientPort {
    List<ShopInfoResponse> getShopInfos(List<Long> shopIds);
}
