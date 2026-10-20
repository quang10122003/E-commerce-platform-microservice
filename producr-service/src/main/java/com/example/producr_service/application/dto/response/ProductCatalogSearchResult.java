package com.example.producr_service.application.dto.response;

import java.util.List;

public record ProductCatalogSearchResult(
        List<ProductSearchResponse> items,
        String nextCursor,
        boolean hasNext,
        List<Long> brandIds
) {
}
