package com.example.producr_service.application.port.in;

import com.example.producr_service.application.dto.response.BrandResponse;

import java.util.List;

public interface GetBrandsUseCase {

    List<BrandResponse> getBrands();
}
