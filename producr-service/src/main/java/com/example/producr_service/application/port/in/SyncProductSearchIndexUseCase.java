package com.example.producr_service.application.port.in;

import com.example.producr_service.application.dto.response.ProductSearchIndexSyncResponse;

public interface SyncProductSearchIndexUseCase {
    ProductSearchIndexSyncResponse syncAllProducts();
}
