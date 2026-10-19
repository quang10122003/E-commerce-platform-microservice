package com.example.producr_service.application.mapper;

import com.example.producr_service.application.dto.response.ShopProductDetailResponse;
import com.example.producr_service.domain.model.AttributeValue;
import com.example.producr_service.domain.model.Product;
import com.example.producr_service.domain.model.ProductAttribute;
import com.example.producr_service.domain.model.ProductVariant;

import java.util.ArrayList;
import java.util.List;

public class ShopProductDetailMapper {
    public ShopProductDetailResponse toShopProductDetailResponse(Product product) {
        List<ProductAttribute> sourceAttributes = product.getAttributes();
        List<ShopProductDetailResponse.Attribute> attributes = sourceAttributes.stream()
                .map(attribute -> new ShopProductDetailResponse.Attribute(
                        attribute.getId(), attribute.getName(),
                        attribute.getValues().stream()
                                .map(value -> new ShopProductDetailResponse.Value(
                                        value.getId(), value.getValue()))
                                .toList()))
                .toList();
        List<ShopProductDetailResponse.Variant> variants = product.getVariants().stream()
                .map(variant -> toShopVariantResponse(variant, sourceAttributes))
                .toList();
        return new ShopProductDetailResponse(product.getId(), product.getCategoryId(),
                product.getBrandId(), product.getName(), product.getDescription(),
                product.getImageUrl(), attributes, variants);
    }

    // Form gửi tổ hợp bằng vị trí nên phải giữ đúng thứ tự giá trị trong thuộc tính.
    private ShopProductDetailResponse.Variant toShopVariantResponse(
            ProductVariant variant, List<ProductAttribute> attributes) {
        List<ShopProductDetailResponse.Selection> selections = new ArrayList<>();
        for (int attributeIndex = 0; attributeIndex < attributes.size(); attributeIndex++) {
            List<AttributeValue> values = attributes.get(attributeIndex).getValues();
            for (int valueIndex = 0; valueIndex < values.size(); valueIndex++) {
                if (variant.getAttributeValues().contains(values.get(valueIndex))) {
                    selections.add(new ShopProductDetailResponse.Selection(attributeIndex, valueIndex));
                }
            }
        }
        return new ShopProductDetailResponse.Variant(
                variant.getId(), variant.getSku(), variant.getPrice().getAmount(),
                variant.getStockQuantity(), selections,
                variant.getImages().stream()
                        .map(image -> new ShopProductDetailResponse.Image(
                                image.getId(), image.getImageUrl(), image.isPrimary()))
                        .toList());
    }
}
