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

// Mã hóa giá trị sắp xếp của bản ghi cuối trang thành cursor để lấy trang kế tiếp bằng search_after.
@Component
@RequiredArgsConstructor
public class ProductSearchCursorCodec {
    private final ObjectMapper objectMapper;

    // Số giá trị sắp xếp của từng kiểu, luôn kết thúc bằng productId làm khóa phụ.
    private static int sortValueCount(ProductSortOption sortOption) {
        return sortOption == ProductSortOption.HIGHEST_RATED ? 3 : 2;
    }

    public String encode(List<Object> sortValues, ProductSortOption sortOption) {
        if (sortValues == null || sortValues.size() != sortValueCount(sortOption)
                || !(sortValues.getLast() instanceof Number)) {
            throw new IllegalArgumentException("Cursor sort values khong hop le");
        }
        try {
            byte[] json = objectMapper.writeValueAsBytes(new ProductScrollCursor(sortOption, sortValues));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(json);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Khong the ma hoa cursor product", exception);
        }
    }

    public List<Object> decode(String encodedCursor, ProductSortOption sortOption) {
        try {
            byte[] json = Base64.getUrlDecoder().decode(encodedCursor.getBytes(StandardCharsets.UTF_8));
            ProductScrollCursor cursor = objectMapper.readValue(json, ProductScrollCursor.class);
            if (cursor.sortOption() != sortOption || cursor.sortValues() == null
                    || cursor.sortValues().size() != sortValueCount(sortOption)
                    || cursor.sortValues().contains(null)
                    || !(cursor.sortValues().getLast() instanceof Number)) {
                throw new IllegalArgumentException("Cursor product khong hop le");
            }
            return cursor.sortValues();
        } catch (IllegalArgumentException | IOException exception) {
            throw new IllegalArgumentException("Cursor product khong hop le", exception);
        }
    }

    public record ProductScrollCursor(ProductSortOption sortOption, List<Object> sortValues) {
    }
}
