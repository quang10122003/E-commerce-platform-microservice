package com.example.producr_service.application.dto.response;

import java.util.List;

// Danh sách brand thuộc toàn bộ tập khớp tìm kiếm, không chỉ trang product hiện tại.
public record ProductCatalogSearchResponse(
        List<ProductSearchResponse> items,
        String nextCursor,
        boolean hasNext,
        List<ProductBrandOption> brands
) {
}
