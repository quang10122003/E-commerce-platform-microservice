package com.example.producr_service.application.dto.response;

import java.util.List;

public record ProductSearchIndexSyncResponse(int totalProducts, int syncedProducts, List<Long> failedProductIds) {
}
