package com.example.producr_service.adapter.DTO.documentElasticsearch;

import com.example.producr_service.application.constant.Constant;
import com.example.producr_service.domain.model.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = Constant.SEARCH_INDEX)
public class ProductSearchDocument {

    @Id
    private Long id;
    // Cursor cần khóa phụ productId để thứ tự ổn định khi các giá trị sort trùng nhau.
    @Field(name = ProductSearchDocumentFields.PRODUCT_ID, type = FieldType.Long)
    private Long productId;

    @Field(name = ProductSearchDocumentFields.USER_ID, type = FieldType.Long)
    private Long userId;

    @Field(name = ProductSearchDocumentFields.NAME, type = FieldType.Text)
    private String name;

    @Field(name = ProductSearchDocumentFields.DESCRIPTION, type = FieldType.Text)
    private String description;

    @Field(name = ProductSearchDocumentFields.LOCATION, type = FieldType.Keyword)
    private String location;

    @Field(name = ProductSearchDocumentFields.CATEGORY_ID, type = FieldType.Long)
    private Long categoryId;

    @Field(name = ProductSearchDocumentFields.CATEGORY_NAME, type = FieldType.Keyword)
    private String categoryName;

    @Field(name = ProductSearchDocumentFields.BRAND_ID, type = FieldType.Long)
    private Long brandId;

    @Field(name = ProductSearchDocumentFields.BRAND_NAME, type = FieldType.Keyword)
    private String brandName;

    @Field(name = ProductSearchDocumentFields.STATUS, type = FieldType.Keyword)
    private ProductStatus status;

    @Field(name = ProductSearchDocumentFields.IMAGE_URL, type = FieldType.Keyword, index = false)
    private String imageUrl;

    @Field(name = ProductSearchDocumentFields.TOTAL_SOLD, type = FieldType.Long)
    private long totalSold;

    @Field(name = ProductSearchDocumentFields.MIN_PRICE, type = FieldType.Double)
    private BigDecimal minPrice;

    @Field(name = ProductSearchDocumentFields.MAX_PRICE, type = FieldType.Double)
    private BigDecimal maxPrice;

    @Field(
            name = ProductSearchDocumentFields.CREATED_AT,
            type = FieldType.Date,
            format = {},
            pattern = "uuuu-MM-dd'T'HH:mm:ss"
    )
    private LocalDateTime createdAt;

    @Field(name = ProductSearchDocumentFields.ATTRIBUTES, type = FieldType.Nested)
    private List<AttributeDocument> attributes;

    @Field(name = ProductSearchDocumentFields.VARIANTS, type = FieldType.Nested)
    private List<VariantDocument> variants;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AttributeDocument {

        @Field(type = FieldType.Keyword)
        private String name;

        @Field(type = FieldType.Keyword)
        private String value;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VariantDocument {

        @Field(type = FieldType.Keyword)
        private String sku;

        @Field(type = FieldType.Double)
        private BigDecimal price;

        @Field(type = FieldType.Integer)
        private int stockQuantity;
    }
}
