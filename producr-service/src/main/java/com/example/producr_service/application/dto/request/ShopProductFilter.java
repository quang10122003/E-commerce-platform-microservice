package com.example.producr_service.application.dto.request;

import com.example.common.exception.BusinessException;
import com.example.common.response.PageQuery;
import com.example.producr_service.application.error.ProductError;

public record ShopProductFilter(PageQuery pageQuery, String keyword, Long categoryId,
                                ShopProductSortOption sortOption) {
    public ShopProductFilter {
        // Phân trang offset không được vượt result window mặc định của ES.
        if ((long) pageQuery.page() * pageQuery.size() > 10_000) {
            throw new BusinessException(ProductError.PRODUCT_SEARCH_PAGE_LIMIT_EXCEEDED);
        }
        keyword = keyword == null ? null : keyword.trim();
        sortOption = sortOption == null ? ShopProductSortOption.RELEVANCE : sortOption;
    }
}
