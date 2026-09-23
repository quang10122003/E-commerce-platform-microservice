package com.example.producr_service.application.port.in;

import com.example.producr_service.application.dto.request.ProductScrollFilter;
import com.example.producr_service.application.dto.response.ProductCatalogSearchResponse;

public interface GetProductsCatalogUseCase {
    ProductCatalogSearchResponse getProducts(ProductScrollFilter filter);
}
