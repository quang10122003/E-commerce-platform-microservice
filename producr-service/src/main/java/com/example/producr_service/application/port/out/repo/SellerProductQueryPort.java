package com.example.producr_service.application.port.out.repo;

import com.example.common.response.PageResponse;
import com.example.producr_service.application.dto.request.SellerProductFilter;
import com.example.producr_service.application.dto.response.SellerProductItemResponse;
import com.example.producr_service.domain.model.Product;
import com.example.producr_service.domain.model.ProductVariant;

import java.util.List;

public interface SellerProductQueryPort {
    // L?y m?t trang s?n ph?m thu?c ??ng ng??i b?n ???c x?c ??nh t? phi?n ??ng nh?p.
    PageResponse<SellerProductItemResponse> findProducts(
            Long sellerUserId,
            SellerProductFilter filter
    );



}
