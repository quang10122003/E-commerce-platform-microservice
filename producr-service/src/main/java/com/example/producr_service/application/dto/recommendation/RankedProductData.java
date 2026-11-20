package com.example.producr_service.application.dto.recommendation;

import com.example.producr_service.application.dto.response.ProductSearchResponse;

public record RankedProductData(ProductSearchResponse product, double score) {}
