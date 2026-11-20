package com.example.producr_service.application.port.in;

import com.example.producr_service.application.dto.response.BrandResponse;

public interface CreateBrandUseCase {

    BrandResponse createBrand(String name);
}
