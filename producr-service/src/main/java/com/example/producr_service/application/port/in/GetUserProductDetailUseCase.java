package com.example.producr_service.application.port.in;

import com.example.producr_service.application.dto.response.UserProductDetailResponse;

public interface GetUserProductDetailUseCase {
    // Đọc product cùng trạng thái và tổ hợp variant cho trang chi tiết công khai.
    UserProductDetailResponse getUserProductDetail(Long productId);
}
