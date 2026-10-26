package com.example.producr_service.adapter.DTO.documentElasticsearch;

import com.example.producr_service.application.constant.Constant;
import org.springframework.data.annotation.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = Constant.PRODUCT_EMBEDDING_INDEX)
public class ProductEmbeddingDocument {
    @Id
    @Field(type = FieldType.Long)
    private Long productId;

    @Field(
            type = FieldType.Dense_Vector,
            dims = Constant.PRODUCT_EMBEDDING_DIMENSIONS,
            index = true,
            similarity = "cosine"
    )
    private List<Float> vector;

    @Field(type = FieldType.Keyword)
    private String contentHash;
}
