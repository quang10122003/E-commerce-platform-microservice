package com.example.producr_service.application.dto.request;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

public record ProductScrollFilter(
        String keyword,
        Long categoryId,
        List<Long> brandIds,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        String cursor,
        List<Integer> locationCodes,
        int size,
        ProductSortOption sortOption
) {

    public ProductScrollFilter {
        sortOption = sortOption == null ? ProductSortOption.RELEVANCE : sortOption;
        locationCodes = locationCodes == null
                ? List.of()
                : locationCodes.stream()
                        .filter(Objects::nonNull)
                        .distinct()
                        .toList();
        brandIds = brandIds == null
                ? List.of()
                : brandIds.stream()
                        .filter(Objects::nonNull)
                        .distinct()
                        .toList();
    }
}
