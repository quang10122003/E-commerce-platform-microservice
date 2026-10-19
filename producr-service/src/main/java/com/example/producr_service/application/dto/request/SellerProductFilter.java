package com.example.producr_service.application.dto.request;

import com.example.common.response.PageQuery;
public record SellerProductFilter(PageQuery pageQuery,
                                  Long categoryId,
                                  Status status,
                                  String keyword) {

    public enum Status {
        ACTIVE,
        OUT_OF_STOCK,
        INACTIVE
    }
}
