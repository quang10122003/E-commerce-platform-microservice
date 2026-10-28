package com.example.producr_service.application.dto.response;

public record SellerProductOverviewResponse(long totalProducts, long activeProducts,
                                            long outOfStockProducts, long inactiveProducts) {
}
