package com.example.producr_service.application.port.in;

import com.example.producr_service.application.dto.response.UserProductDetailResponse;

public interface GetUserProductDetailUseCase {
    UserProductDetailResponse getUserProductDetail(Long productId);
}
