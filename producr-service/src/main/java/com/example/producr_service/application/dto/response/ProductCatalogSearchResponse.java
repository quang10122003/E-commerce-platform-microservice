package com.example.producr_service.application.dto.response;

import java.util.List;

/**
 * Kết quả catalog gồm product theo cursor và các brand của toàn bộ tập match.
 */
public record ProductCatalogSearchResponse(
        List<ProductSearchResponse> items,
        String nextCursor,
        boolean hasNext,
        List<ProductBrandOption> brands
) {
}
