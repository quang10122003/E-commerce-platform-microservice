package com.example.producr_service.application.service;

import com.example.common.response.PageResponse;
import com.example.producr_service.application.dto.response.SellerProductItemResponse;
import com.example.producr_service.application.dto.response.ShopFlashSaleProductResponse;
import com.example.producr_service.domain.model.AttributeValue;
import com.example.producr_service.domain.model.Product;
import com.example.producr_service.domain.model.ProductAttribute;
import com.example.producr_service.domain.model.ProductStatus;
import com.example.producr_service.domain.model.ProductVariant;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ShopProductPageAssembler {

    public List<Long> getListIdProduct(Collection<Product> products) {
        return products.stream().map(Product::getId).toList();
    }

    public PageResponse<SellerProductItemResponse> buildSellerProductPage(
            PageResponse<Product> productPage,
            Map<Long, List<ProductVariant>> variantsByProductId
    ) {
        List<SellerProductItemResponse> items = buildListSellerProductItemResponse(
                productPage.items(), variantsByProductId);
        return new PageResponse<>(items, productPage.page(), productPage.pageSize(),
                productPage.totalItems(), productPage.totalPages());
    }

    public List<SellerProductItemResponse> buildListSellerProductItemResponse(
            List<Product> products,
            Map<Long, List<ProductVariant>> variantsByProductId
    ) {
        return products.stream()
                .map(product -> toSellerItem(product,
                        variantsByProductId.getOrDefault(product.getId(), List.of())))
                .toList();
    }

    public List<ShopFlashSaleProductResponse> buildFlashSaleProducts(
            List<Product> products, Map<Long, List<ProductVariant>> variantsByProductId) {
        return buildListSellerProductItemResponse(products, variantsByProductId).stream()
                .map(product -> new ShopFlashSaleProductResponse(product.id(), product.name(),
                        product.categoryName(), product.status(), product.variants()))
                .toList();
    }

    private SellerProductItemResponse toSellerItem(Product product, List<ProductVariant> variants) {
        Map<Long, ProductAttribute> attributeByValueId = product.getAttributes().stream()
                .flatMap(attribute -> attribute.getValues().stream()
                        .map(value -> Map.entry(value.getId(), attribute)))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        List<SellerProductItemResponse.Variant> variantResponses = variants.stream()
                .sorted(Comparator.comparing(ProductVariant::getId))
                .map(variant -> toSellerVariant(variant, attributeByValueId))
                .toList();

        // Hết hàng được ưu tiên hơn trạng thái ẩn để phản ánh khả năng bán thực tế.
        boolean inStock = variants.stream().anyMatch(ProductVariant::isInStock);
        SellerProductItemResponse.DisplayStatus displayStatus = !inStock
                ? SellerProductItemResponse.DisplayStatus.OUT_OF_STOCK
                : product.getStatus() == ProductStatus.INACTIVE
                        ? SellerProductItemResponse.DisplayStatus.INACTIVE
                        : SellerProductItemResponse.DisplayStatus.ACTIVE;

        return new SellerProductItemResponse(
                product.getId(), product.getName(), product.getImageUrl(), product.getCreatedAt(),
                product.getCategoryId(), product.getCategoryName(), displayStatus, variantResponses);
    }

    private SellerProductItemResponse.Variant toSellerVariant(
            ProductVariant variant,
            Map<Long, ProductAttribute> attributeByValueId
    ) {
        List<SellerProductItemResponse.VariantAttribute> attributes = variant.getAttributeValues().stream()
                .map(value -> toSellerVariantAttribute(value, attributeByValueId))
                .sorted(Comparator.comparing(SellerProductItemResponse.VariantAttribute::attributeId))
                .toList();

        return new SellerProductItemResponse.Variant(
                variant.getId(), variant.getSku(), variant.getPrice().getAmount(),
                variant.getStockQuantity(), attributes);
    }

    private SellerProductItemResponse.VariantAttribute toSellerVariantAttribute(
            AttributeValue value,
            Map<Long, ProductAttribute> attributeByValueId
    ) {
        ProductAttribute attribute = attributeByValueId.get(value.getId());
        if (attribute == null) {
            throw new IllegalStateException("Không tìm thấy thuộc tính của giá trị id=" + value.getId());
        }
        return new SellerProductItemResponse.VariantAttribute(
                attribute.getId(), attribute.getName(), value.getValue());
    }
}
