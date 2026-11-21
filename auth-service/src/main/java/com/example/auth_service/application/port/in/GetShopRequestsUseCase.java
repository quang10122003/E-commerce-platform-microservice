package com.example.auth_service.application.port.in;

import com.example.auth_service.application.DTO.repone.AdminShopRequestResponse;
import com.example.auth_service.domain.model.ShopRequestStatus;
import com.example.common.response.PageQuery;
import com.example.common.response.PageResponse;

public interface GetShopRequestsUseCase {
    PageResponse<AdminShopRequestResponse> getShopRequests(ShopRequestStatus status, String keyword, PageQuery pageQuery);
}
