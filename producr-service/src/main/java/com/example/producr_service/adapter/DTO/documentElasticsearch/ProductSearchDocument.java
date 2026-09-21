package com.example.producr_service.adapter.DTO.documentElasticsearch;
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
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "product_search")
//cấu trúc index cho product
public class ProductSearchDocument {

    // Lưu ID Product từ MySQL làm ID document Elasticsearch.
    @Id
    private Long id;

    // Lưu tên Product để tìm kiếm theo từ khóa.
    @Field(type = FieldType.Text)
    private String name;

    // Lưu mô tả Product để mở rộng kết quả tìm kiếm.
    @Field(type = FieldType.Text)
    private String description;

    // Lưu ID category để lọc Product theo danh mục.
    @Field(type = FieldType.Long)
    private Long categoryId;

    // Lưu ID brand để lọc Product theo thương hiệu.
    @Field(type = FieldType.Long)
    private Long brandId;

    // Lưu trạng thái product để lọc kết quả tìm kiếm.
    @Field(type = FieldType.Keyword)
    private ProductStatus status;

    // Lưu URL ảnh đại diện để hiển thị Product card.
    @Field(type = FieldType.Keyword, index = false)
    private String imageUrl;

    // Lưu giá thấp nhất để hiển thị và sắp xếp Product.
    @Field(type = FieldType.Double)
    private BigDecimal minPrice;

    // Lưu giá cao nhất để hiển thị khoảng giá Product.
    @Field(type = FieldType.Double)
    private BigDecimal maxPrice;

    // Lưu danh sách thuộc tính để lọc theo màu, kích thước.
    @Field(type = FieldType.Nested)
    private List<AttributeDocument> attributes;

    // Lưu các variant để tìm SKU và lọc giá chính xác.
    @Field(type = FieldType.Nested)
    private List<VariantDocument> variants;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AttributeDocument {

        // Lưu tên thuộc tính, ví dụ: Màu sắc.
        @Field(type = FieldType.Keyword)
        private String name;

        // Lưu giá trị thuộc tính, ví dụ: Đen.
        @Field(type = FieldType.Keyword)
        private String value;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VariantDocument {

        // Lưu SKU để tìm chính xác variant.
        @Field(type = FieldType.Keyword)
        private String sku;

        // Lưu giá của từng variant để filter giá chính xác.
        @Field(type = FieldType.Double)
        private BigDecimal price;

        // Lưu tồn kho để sau này lọc variant còn hàng.
        @Field(type = FieldType.Integer)
        private int stockQuantity;
    }
}
