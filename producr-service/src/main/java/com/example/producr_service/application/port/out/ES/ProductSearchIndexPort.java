package com.example.producr_service.application.port.out.ES;

import com.example.producr_service.application.dto.request.ProductScrollFilter;
import com.example.common.response.PageResponse;
import com.example.producr_service.application.dto.request.ShopProductFilter;
import com.example.producr_service.application.dto.response.ProductSearchResponse;
import com.example.producr_service.application.dto.response.ProductCatalogSearchResult;
import com.example.producr_service.domain.model.Product;

public interface ProductSearchIndexPort {
    void index(Product product);

    void deleteById(Long productId);

    ProductCatalogSearchResult search(ProductScrollFilter filter);

    PageResponse<ProductSearchResponse> searchShopProducts(Long shopId, ShopProductFilter filter);
}
