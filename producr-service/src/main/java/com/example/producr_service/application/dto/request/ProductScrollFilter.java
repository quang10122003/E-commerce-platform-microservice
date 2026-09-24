package com.example.producr_service.application.dto.request;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

//  điều kiện tìm kiếm product và cursor cho infinity scroll.
public record ProductScrollFilter(
        String keyword,
        Long categoryId,
        List<Long> brandIds,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        String cursor,
        List<String> locations,
        int size,
        ProductSortOption sortOption
) {

    // Dùng tiêu chí liên quan khi client không truyền lựa chọn sắp xếp.
    public ProductScrollFilter {
        sortOption = sortOption == null ? ProductSortOption.RELEVANCE : sortOption;
        locations = locations == null
                ? List.of()
                : locations.stream()
                        .filter(Objects::nonNull)
                        .map(String::trim)
                        .filter(location -> !location.isBlank())
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
