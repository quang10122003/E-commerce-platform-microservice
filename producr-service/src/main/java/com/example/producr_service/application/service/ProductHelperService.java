package com.example.producr_service.application.service;

import com.example.producr_service.adapter.entity.ProductVariantEntity;
import com.example.producr_service.application.dto.response.SellerProductItemResponse;
import com.example.producr_service.domain.model.Product;
import com.example.producr_service.domain.model.ProductVariant;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ProductHelperService {
    // laáº¥y danh saách id service tá»« list product
    public List<Long> getListIdProduct(Collection<Product> listProduct){
        return listProduct.stream().map(Product::getId).toList();
    }
    // build  List<SellerProductItemResponse>
    private buildListSellerProductItemResponse()
    List<SellerProductItemResponse> items = productPage.getContent().stream()
            .map(product -> productMapper.toSellerItem(
                    product,
                    variantsByProductId.getOrDefault(product.getId(), List.of())
            ))
            .toList();


}
