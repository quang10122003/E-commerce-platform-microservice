package com.example.producr_service.application.dto.request;

import com.example.common.response.PageQuery;
// Chứa điều kiện lọc danh sách sản phẩm của người bán.
public record SellerProductFilter(PageQuery pageQuery,
                                  Long categoryId,
                                  Status status,
                                  String keywork) {

    // Xác định nhóm sản phẩm cần lọc trên màn quản lý.
    public enum Status {
        ACTIVE,
        OUT_OF_STOCK,
        INACTIVE
    }
}
