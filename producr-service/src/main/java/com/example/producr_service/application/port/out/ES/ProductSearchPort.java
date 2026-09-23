package com.example.producr_service.application.port.out.ES;

import com.example.producr_service.application.dto.request.ProductScrollFilter;
import com.example.producr_service.application.dto.response.ProductCatalogSearchResponse;

public interface ProductSearchPort {
    ProductCatalogSearchResponse search(ProductScrollFilter filter);
}
