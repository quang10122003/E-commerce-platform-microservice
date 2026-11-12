package com.example.producr_service.application.port.in;

import com.example.common.response.PageResponse;
import com.example.producr_service.application.dto.response.BrandResponse;

public interface GetAdminBrandsUseCase {

    PageResponse<BrandResponse> getAdminBrands(int page, int size, String keyword);
}
