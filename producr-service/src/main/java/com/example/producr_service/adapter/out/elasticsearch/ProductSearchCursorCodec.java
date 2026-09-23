package com.example.producr_service.adapter.out.elasticsearch;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.producr_service.application.dto.request.ProductSortOption;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

// Mã hóa và giải mã cursor dựa trên các giá trị sort của Elasticsearch.
@Component
@RequiredArgsConstructor
public class ProductSearchCursorCodec {

    private final ObjectMapper objectMapper;

    // Mã hóa giá trị sort của item cuối thành cursor an toàn cho URL.
    public String encode(List<Object> sortValues, ProductSortOption sortOption) {
        if (sortValues == null || sortValues.size() != 2) {
            throw new IllegalArgumentException("Cursor sort values khong hop le");
        }

        ProductScrollCursor cursor = new ProductScrollCursor(
                sortOption,
                sortValues.get(0),
                ((Number) sortValues.get(1)).longValue()
        );

        try {
            byte[] json = objectMapper.writeValueAsBytes(cursor);
            return Base64.getUrlEncoder().withoutPadding().encodeToString(json);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Khong the ma hoa cursor product", exception);
        }
    }

    // Giải mã cursor của client thành giá trị search_after cho Elasticsearch.
    public List<Object> decode(String encodedCursor, ProductSortOption sortOption) {
        try {
            byte[] json = Base64.getUrlDecoder().decode(encodedCursor.getBytes(StandardCharsets.UTF_8));
            ProductScrollCursor cursor = objectMapper.readValue(json, ProductScrollCursor.class);

            if (cursor.sortOption() != sortOption
                    || cursor.sortValue() == null
                    || cursor.productId() == null) {
                throw new IllegalArgumentException("Cursor product khong hop le");
            }

            return List.of(cursor.sortValue(), cursor.productId());
        } catch (IllegalArgumentException | IOException exception) {
            throw new IllegalArgumentException("Cursor product khong hop le", exception);
        }
    }

    // Lưu hai giá trị sort cần thiết để lấy batch product tiếp theo.
    public record ProductScrollCursor(
            ProductSortOption sortOption,
            Object sortValue,
            Long productId
    ) {
    }
}
