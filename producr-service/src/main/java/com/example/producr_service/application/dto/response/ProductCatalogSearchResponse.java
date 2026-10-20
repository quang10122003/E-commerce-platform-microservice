package com.example.producr_service.application.dto.response;

import java.util.List;

// Trả tối đa 50 brand có nhiều product khớp nhất, không chỉ tính trang product hiện tại.
public record ProductCatalogSearchResponse(
        List<ProductSearchResponse> items,
        String nextCursor,
        boolean hasNext,
        List<ProductBrandOption> brands
) {
}
