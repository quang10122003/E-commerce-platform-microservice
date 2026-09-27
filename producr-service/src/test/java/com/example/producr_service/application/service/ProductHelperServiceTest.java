package com.example.producr_service.application.service;

import com.example.common.response.PageResponse;
import com.example.producr_service.application.dto.response.SellerProductItemResponse;
import com.example.producr_service.domain.model.AttributeValue;
import com.example.producr_service.domain.model.Money;
import com.example.producr_service.domain.model.Product;
import com.example.producr_service.domain.model.ProductAttribute;
import com.example.producr_service.domain.model.ProductVariant;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProductHelperServiceTest {

    // Kiểm tra phân loại giữ đúng thuộc tính và trạng thái hết hàng ưu tiên hơn đang ẩn.
    @Test
    void buildsSellerPageFromProductsAndVariants() {
        Product product = new Product(1L, 7L, 2L, "Điện thoại", null, null,
                "Điện thoại mẫu", null, "https://example.com/product.jpg");
        product.deactivate();

        ProductAttribute color = new ProductAttribute(11L, "Màu sắc");
        AttributeValue red = new AttributeValue(12L, "Đỏ");
        color.addValue(red);
        product.addAttribute(color);

        ProductVariant variant = new ProductVariant(21L, "PHONE-RED",
                Money.of(BigDecimal.valueOf(100_000)), 0);
        variant.linkAttributeValue(new AttributeValue(12L, "Đỏ"));

        PageResponse<Product> productPage = new PageResponse<>(List.of(product), 3, 10, 25, 3);
        PageResponse<SellerProductItemResponse> result = new ProductHelperService()
                .buildSellerProductPage(productPage, Map.of(1L, List.of(variant)));

        assertEquals(3, result.page());
        assertEquals(25, result.totalItems());
        assertEquals(3, result.totalPages());
        assertEquals("Điện thoại", result.items().getFirst().categoryName());
        assertEquals(SellerProductItemResponse.DisplayStatus.OUT_OF_STOCK,
                result.items().getFirst().status());
        assertEquals(new SellerProductItemResponse.VariantAttribute(11L, "Màu sắc", "Đỏ"),
                result.items().getFirst().variants().getFirst().attributes().getFirst());
    }
}
