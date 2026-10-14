package com.example.producr_service.application.mapper;

import com.example.producr_service.application.dto.response.UserProductDetailResponse;
import com.example.producr_service.domain.model.AttributeValue;
import com.example.producr_service.domain.model.Product;
import com.example.producr_service.domain.model.ProductAttribute;
import com.example.producr_service.domain.model.ProductVariant;
import com.example.producr_service.domain.model.VariantImage;
import java.util.Comparator;
import java.util.List;

// Mapper thuần Java cho hợp đồng chi tiết công khai, không thay DTO quản lý sản phẩm.
public final class UserProductDetailMapper {
    private UserProductDetailMapper() {}

    // Dựng dữ liệu tổ hợp và chọn variant giá cao nhất, không loại variant hết hàng.
    public static UserProductDetailResponse toResponse(Product product, String location) {
        Long defaultVariantId = product.getVariants().stream()
                .sorted(Comparator.comparing((ProductVariant variant) -> variant.getPrice().getAmount())
                        .reversed().thenComparing(ProductVariant::getId))
                .map(ProductVariant::getId)
                .findFirst().orElse(null);

        List<UserProductDetailResponse.Attribute> attributes = product.getAttributes().stream()
                .sorted(Comparator.comparing(ProductAttribute::getId))
                .map(attribute -> new UserProductDetailResponse.Attribute(
                        attribute.getId(), attribute.getName(),
                        attribute.getValues().stream().sorted(Comparator.comparing(AttributeValue::getId))
                                .map(value -> new UserProductDetailResponse.Value(value.getId(), value.getValue()))
                                .toList()))
                .toList();

        return new UserProductDetailResponse(
                product.getId(), product.getUserId(), product.getName(), product.getDescription(),
                product.getImageUrl(), product.getStatus(), location, product.getTotalSold(), defaultVariantId, attributes,
                product.getVariants().stream().sorted(Comparator.comparing(ProductVariant::getId))
                        .map(UserProductDetailMapper::toVariantResponse).toList()
        );
    }

    // Dùng quy tắc tồn kho của domain và giữ ID giá trị để FE đối chiếu tổ hợp.
    private static UserProductDetailResponse.Variant toVariantResponse(ProductVariant variant) {
        return new UserProductDetailResponse.Variant(
                variant.getId(), variant.getPrice().getAmount(), variant.getStockQuantity(),
                variant.isInStock() ? UserProductDetailResponse.VariantStatus.AVAILABLE
                        : UserProductDetailResponse.VariantStatus.OUT_OF_STOCK,
                variant.getAttributeValues().stream().map(AttributeValue::getId).sorted().toList(),
                variant.getImages().stream()
                        .sorted(Comparator.comparing(VariantImage::isPrimary).reversed().thenComparing(VariantImage::getId))
                        .map(image -> new UserProductDetailResponse.Image(image.getId(), image.getImageUrl(), image.isPrimary()))
                        .toList()
        );
    }
}
