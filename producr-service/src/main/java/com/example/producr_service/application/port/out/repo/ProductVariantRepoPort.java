package com.example.producr_service.application.port.out.repo;

import com.example.producr_service.domain.model.ProductVariant;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface ProductVariantRepoPort {
    Map<Long, List<ProductVariant>> findByProduct_IdIn(Collection<Long> productIds);
}
