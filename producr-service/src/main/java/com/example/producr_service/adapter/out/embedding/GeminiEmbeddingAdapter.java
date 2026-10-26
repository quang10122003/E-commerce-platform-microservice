package com.example.producr_service.adapter.out.embedding;

import com.example.producr_service.application.port.out.embedding.TextEmbeddingPort;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class GeminiEmbeddingAdapter implements TextEmbeddingPort {
    RestClient restClient;
    String model;
    int dimensions;
    @Override
    public List<Float> embed(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException(
                    "Nội dung tạo embedding không được rỗng");
        }

        Map<String, Object> request = Map.of(
                "content", Map.of(
                        "parts", List.of(
                                Map.of("text", text))),
                "outputDimensionality", dimensions);

        Map<String, Object> response = restClient.post()
                .uri("/models/{model}:embedContent", model)
                .body(request)
                .retrieve()
                // cấu hình chuyển repone trả về thành Map<String, Object>
                .body(new ParameterizedTypeReference<Map<String, Object>>() {});

        return extractVector(response);
    }

    private List<Float> extractVector(Map<String, Object> response) {
        if (response == null
                || !(response.get("embedding") instanceof Map<?, ?> embedding)
                || !(embedding.get("values") instanceof List<?> values)) {
            throw new IllegalStateException(
                    "Gemini không trả về vector embedding hợp lệ");
        }

        if (values.size() != dimensions) {
            throw new IllegalStateException(
                    "Số chiều vector Gemini không khớp cấu hình");
        }

        List<Float> vector = new ArrayList<>(dimensions);
        double squaredNorm = 0;

        for (Object value : values) {
            if (!(value instanceof Number number)) {
                throw new IllegalStateException(
                        "Vector Gemini chứa phần tử không phải số");
            }

            float coordinate = number.floatValue();
            if (!Float.isFinite(coordinate)) {
                throw new IllegalStateException(
                        "Vector Gemini chứa giá trị không hợp lệ");
            }

            vector.add(coordinate);
            squaredNorm += (double) coordinate * coordinate;
        }

        if (squaredNorm == 0) {
            throw new IllegalStateException(
                    "Không thể sử dụng vector toàn số không cho cosine");
        }

        return List.copyOf(vector);
    }

}
